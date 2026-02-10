package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryServiceV1IT extends RegistryApplicationIT {


    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    /*
    Feature: Old endpoint for retrieving DPDS is supported, correctly replacing variables by default
    Notes: Used when the following devOps endpoints are called
           POST /api/v1/pp/devops/activities
           POST /api/v1/pp/devops/activities/{id}/start
           POST /api/v1/pp/devops/activities/{id}/abort
           PUT /api/v1/pp/devops/activities/{id}
           and when an Activity completes/stops (all tasks executed)

    Given a Data Product with id "<productId>" and version "<version>" exists
    When GET /api/v1/pp/registry/products/<productId>/versions/<version>
    Then the Data Product Version descriptor is returned successfully
    And the descriptor has all its variables correctly replaced
    */
    @Test
    void whenGetDataProductVersionThenReturnDescriptorWithVariablesReplaced() throws IOException {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-v1-descriptor");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.v1.descriptor");
        dataProduct.setDisplayName("Test Product V1 Descriptor");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productId = dataProductResponse.getBody().getUuid();

        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-v1-descriptor");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = loadDescriptor("descriptor-with-variables.json");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String versionId = versionResponse.getBody().getUuid();
        String version = versionResponse.getBody().getVersionNumber();

        DescriptorVariableRes apiUrlVariable = new DescriptorVariableRes();
        apiUrlVariable.setDataProductVersionUuid(versionId);
        apiUrlVariable.setVariableKey("apiUrl");
        apiUrlVariable.setVariableValue("https://api.example.com");

        ResponseEntity<DescriptorVariableRes> apiUrlVariableResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(apiUrlVariable),
                DescriptorVariableRes.class
        );
        assertThat(apiUrlVariableResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long apiUrlVariableId = apiUrlVariableResponse.getBody().getSequenceId();

        DescriptorVariableRes databaseNameVariable = new DescriptorVariableRes();
        databaseNameVariable.setDataProductVersionUuid(versionId);
        databaseNameVariable.setVariableKey("databaseName");
        databaseNameVariable.setVariableValue("production-db");

        ResponseEntity<DescriptorVariableRes> databaseNameVariableResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(databaseNameVariable),
                DescriptorVariableRes.class
        );
        assertThat(databaseNameVariableResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long databaseNameVariableId = databaseNameVariableResponse.getBody().getSequenceId();

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/products/" + productId + "/versions/" + version),
                HttpMethod.GET,
                null,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify the response is valid JSON
        JsonNode descriptorJson = objectMapper.readTree(response.getBody());
        assertThat(descriptorJson).isNotNull();

        // Verify variables are replaced (not present as placeholders)
        String descriptorString = response.getBody();
        assertThat(descriptorString)
                .doesNotContain("${apiUrl}")
                .doesNotContain("${databaseName}")
                .contains("https://api.example.com")
                .contains("production-db");

        // Verify the descriptor has the old structure (DPDS format)
        assertThat(descriptorJson.has("dataProductDescriptor")).isTrue();
        assertThat(descriptorJson.has("info")).isTrue();
        assertThat(descriptorJson.get("info").has("fullyQualifiedName")).isTrue();

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + apiUrlVariableId));
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + databaseNameVariableId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + productId));
    }

    /*
    Feature: Old endpoint for retrieving DPDS is supported, correctly populating raw content
             when format = normalized

    Given a Data Product with id "<productId>" and version "<version>" exists
    When GET /api/v1/pp/registry/products/<productId>/versions/<version>?format=normalized
    Then the Data Product Version descriptor is returned successfully
    And the descriptor has the old structure, with the correct raw contents
    */
    @Test
    void whenGetDataProductVersionThenReturnDescriptorWithRawContent() throws IOException {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-v1-raw-content");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.v1.raw.content");
        dataProduct.setDisplayName("Test Product V1 Raw Content");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productId = dataProductResponse.getBody().getUuid();

        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-v1-raw-content");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.24");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = loadDescriptor("dpds-v1.0.0.json");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String versionId = versionResponse.getBody().getUuid();
        String version = versionResponse.getBody().getVersionNumber();

        // Load expected descriptor structure
        JsonNode expectedDescriptor = loadDescriptor("old/descriptor-with-rawcontent.json");

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/products/" + productId + "/versions/" + version + "?format=normalized"),
                HttpMethod.GET,
                null,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify the response is valid JSON
        JsonNode descriptorJson = objectMapper.readTree(response.getBody());
        assertThat(descriptorJson).isNotNull();
        assertThat(descriptorJson)
                .usingRecursiveComparison()
                .isEqualTo(expectedDescriptor);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + productId));
    }

}

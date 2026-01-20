package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.resolve.ResolveDataProductVersionCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.resolve.ResolveDataProductVersionResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableResultRes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DataProductVersionVariablesResolverUseCaseControllerIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    /*
     * Scenario: Successful Variable Resolution
     *
     * Given a Data Product Version with a descriptor containing variable placeholders (e.g., ${variableName})
     * And the descriptor uses a supported specification version
     * And all required descriptor variables for the Data Product Version have been stored
     * When the RESOLVE use case is executed
     * Then the descriptor is correctly populated with the resolved variable values
     */
    @Test
    public void whenResolveDataProductVersionWithAllVariablesStoredThenReturnResolvedContent() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-resolve-success");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.resolve.success");
        dataProduct.setDisplayName("Test Product for Resolve Success");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();


        // Create data product version with ODM spec and descriptor containing variable placeholders
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-resolve-success");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("DPDS");
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


        // Store all required descriptor variables
        List<DescriptorVariableRes> variables = new ArrayList<>();
        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(versionId);
        variable1.setVariableKey("apiUrl");
        variable1.setVariableValue("https://api.example.com");
        variables.add(variable1);

        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(versionId);
        variable2.setVariableKey("databaseName");
        variable2.setVariableValue("production-db");
        variables.add(variable2);

        StoreDescriptorVariableCommandRes storeCommand = new StoreDescriptorVariableCommandRes();
        storeCommand.setDescriptorVariables(variables);

        ResponseEntity<StoreDescriptorVariableResultRes> storeResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(storeCommand),
                StoreDescriptorVariableResultRes.class
        );
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List<DescriptorVariableRes> storedVariables = storeResponse.getBody().getDescriptorVariables();


        // When - Resolve variables
        ResolveDataProductVersionCommandRes resolveCommand = new ResolveDataProductVersionCommandRes();
        resolveCommand.setDataProductVersionUuid(versionId);

        ResponseEntity<ResolveDataProductVersionResultRes> resolveResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/resolve"),
                new HttpEntity<>(resolveCommand),
                ResolveDataProductVersionResultRes.class
        );

        // Then
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody()).isNotNull();
        assertThat(resolveResponse.getBody().getDataProductVersion()).isNotNull();
        assertThat(resolveResponse.getBody().getDataProductVersion().getResolvedContent()).isNotNull();

        JsonNode resolvedContent = resolveResponse.getBody().getDataProductVersion().getResolvedContent();
        String resolvedContentString = objectMapper.writeValueAsString(resolvedContent);
        assertThat(resolvedContentString).contains("https://api.example.com");
        assertThat(resolvedContentString).contains("production-db");
        assertThat(resolvedContentString).doesNotContain("${apiUrl}");
        assertThat(resolvedContentString).doesNotContain("${databaseName}");

        // Cleanup descriptor variables
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }

        // Cleanup data product version
        rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                HttpMethod.DELETE,
                null,
                Void.class
        );


        // Cleanup data product
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    
    }

    /*
     * Scenario: Variable Resolution with Unsupported Specification Version
     *
     * Given a Data Product Version with a descriptor
     * And the descriptor uses an unsupported specification version
     * And some descriptor variables are stored for the Data Product Version
     * When the RESOLVE use case is executed
     * Then a BadRequestException is thrown indicating the unsupported version
     */
    @Test
    public void whenResolveDataProductVersionWithUnsupportedSpecThenReturnBadRequest() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-resolve-unsupported");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.resolve.unsupported");
        dataProduct.setDisplayName("Test Product for Resolve Unsupported");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create data product version with unsupported spec
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-resolve-unsupported");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("CUSTOM"); // Unsupported specification
        dataProductVersion.setSpecVersion("2.0.0");
        JsonNode content = loadDescriptor("dpds-v1.0.0.json");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String versionId = versionResponse.getBody().getUuid();

        // Store some descriptor variables
        List<DescriptorVariableRes> variables = new ArrayList<>();
        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(versionId);
        variable1.setVariableKey("anyKey1");
        variable1.setVariableValue("anyValue1");
        variables.add(variable1);

        StoreDescriptorVariableCommandRes storeCommand = new StoreDescriptorVariableCommandRes();
        storeCommand.setDescriptorVariables(variables);

        ResponseEntity<StoreDescriptorVariableResultRes> storeResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(storeCommand),
                StoreDescriptorVariableResultRes.class
        );
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List<DescriptorVariableRes> storedVariables = storeResponse.getBody().getDescriptorVariables();

        // When - Resolve variables
        ResolveDataProductVersionCommandRes resolveCommand = new ResolveDataProductVersionCommandRes();
        resolveCommand.setDataProductVersionUuid(versionId);

        ResponseEntity<String> resolveResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/resolve"),
                new HttpEntity<>(resolveCommand),
                String.class
        );

        // Then
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resolveResponse.getBody()).isNotNull();
        assertThat(resolveResponse.getBody()).contains("Unsupported specification");
        assertThat(resolveResponse.getBody()).contains("CUSTOM");
        assertThat(resolveResponse.getBody()).contains("2.0.0");

        // Cleanup descriptor variables
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }

        // Cleanup data product version
        rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Cleanup data product
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    /*
     * Scenario: Variable Resolution with Missing Required Variables
     *
     * Given a Data Product Version with a descriptor containing variable placeholders
     * And the descriptor uses a supported specification version
     * And some required descriptor variables are missing for the Data Product Version
     * When the RESOLVE use case is executed
     * Then the descriptor is correctly populated only with the variables that have a value
     */
    @Test
    public void whenResolveDataProductVersionWithMissingVariablesThenReturnPartiallyResolvedContent() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-resolve-missing");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.resolve.missing");
        dataProduct.setDisplayName("Test Product for Resolve Missing");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create data product version with ODM spec and descriptor containing multiple variable placeholders
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-resolve-missing");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("DPDS");
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

        // Store only some descriptor variables (missingVar is not stored)
        List<DescriptorVariableRes> variables = new ArrayList<>();
        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(versionId);
        variable1.setVariableKey("apiUrl");
        variable1.setVariableValue("https://api.example.com");
        variables.add(variable1);

        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(versionId);
        variable2.setVariableKey("databaseName");
        variable2.setVariableValue("production-db");
        variables.add(variable2);

        StoreDescriptorVariableCommandRes storeCommand = new StoreDescriptorVariableCommandRes();
        storeCommand.setDescriptorVariables(variables);

        ResponseEntity<StoreDescriptorVariableResultRes> storeResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(storeCommand),
                StoreDescriptorVariableResultRes.class
        );
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List<DescriptorVariableRes> storedVariables = storeResponse.getBody().getDescriptorVariables();

        // When - Resolve variables
        ResolveDataProductVersionCommandRes resolveCommand = new ResolveDataProductVersionCommandRes();
        resolveCommand.setDataProductVersionUuid(versionId);

        ResponseEntity<ResolveDataProductVersionResultRes> resolveResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/resolve"),
                new HttpEntity<>(resolveCommand),
                ResolveDataProductVersionResultRes.class
        );

        // Then
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody()).isNotNull();
        assertThat(resolveResponse.getBody().getDataProductVersion()).isNotNull();
        assertThat(resolveResponse.getBody().getDataProductVersion().getResolvedContent()).isNotNull();

        JsonNode resolvedContent = resolveResponse.getBody().getDataProductVersion().getResolvedContent();
        String resolvedContentString = objectMapper.writeValueAsString(resolvedContent);
        assertThat(resolvedContentString).contains("https://api.example.com");
        assertThat(resolvedContentString).contains("production-db");
        assertThat(resolvedContentString).doesNotContain("${apiUrl}");
        assertThat(resolvedContentString).doesNotContain("${databaseName}");
        // Missing variable placeholder should remain unresolved
        assertThat(resolvedContentString).contains("${missingVar}");

        // Cleanup descriptor variables
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }

        // Cleanup data product version
        rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Cleanup data product
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    /*
     * Scenario: Variable Resolution with Variable Value containing invalid JSON
     *
     * Given a Data Product Version with a descriptor containing variable placeholders
     * And the descriptor uses a supported specification version
     * And some descriptor variables have a value containing invalid JSON
     * When the RESOLVE use case is executed
     * Then the descriptor is correctly populated and the variable with the invalid JSON are escaped
     */
    @Test
    public void whenResolveDataProductVersionWithInvalidJsonInVariableThenReturnEscapedContent() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-resolve-invalid-json");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.resolve.invalid.json");
        dataProduct.setDisplayName("Test Product for Resolve Invalid JSON");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create data product version with ODM spec and descriptor containing variable placeholders
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-resolve-invalid-json");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("DPDS");
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

        // Store descriptor variables, one with invalid JSON (contains quotes, newlines, etc.)
        List<DescriptorVariableRes> variables = new ArrayList<>();
        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(versionId);
        variable1.setVariableKey("apiUrl");
        variable1.setVariableValue("https://api.example.com");
        variables.add(variable1);

        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(versionId);
        variable2.setVariableKey("invalidJsonVar");
        // Invalid JSON: contains quotes, newlines, backslashes that need escaping
        variable2.setVariableValue("Value with \"quotes\" and\nnewlines and\\backslashes");
        variables.add(variable2);

        StoreDescriptorVariableCommandRes storeCommand = new StoreDescriptorVariableCommandRes();
        storeCommand.setDescriptorVariables(variables);

        ResponseEntity<StoreDescriptorVariableResultRes> storeResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(storeCommand),
                StoreDescriptorVariableResultRes.class
        );
        assertThat(storeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        List<DescriptorVariableRes> storedVariables = storeResponse.getBody().getDescriptorVariables();

        // When - Resolve variables
        ResolveDataProductVersionCommandRes resolveCommand = new ResolveDataProductVersionCommandRes();
        resolveCommand.setDataProductVersionUuid(versionId);

        ResponseEntity<ResolveDataProductVersionResultRes> resolveResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/resolve"),
                new HttpEntity<>(resolveCommand),
                ResolveDataProductVersionResultRes.class
        );

        // Then
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody()).isNotNull();
        assertThat(resolveResponse.getBody().getDataProductVersion()).isNotNull();
        assertThat(resolveResponse.getBody().getDataProductVersion().getResolvedContent()).isNotNull();

        JsonNode resolvedContent = resolveResponse.getBody().getDataProductVersion().getResolvedContent();
        String resolvedContentString = objectMapper.writeValueAsString(resolvedContent);
        assertThat(resolvedContentString).contains("https://api.example.com");
        assertThat(resolvedContentString).doesNotContain("${apiUrl}");
        assertThat(resolvedContentString).doesNotContain("${invalidJsonVar}");
        // The invalid JSON should be properly escaped
        assertThat(resolvedContentString).contains("Value with");
        // Verify the resolved content is valid JSON
        assertThat(resolvedContent).isNotNull();

        // Cleanup descriptor variables
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }

        // Cleanup data product version
        rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Cleanup data product
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

}

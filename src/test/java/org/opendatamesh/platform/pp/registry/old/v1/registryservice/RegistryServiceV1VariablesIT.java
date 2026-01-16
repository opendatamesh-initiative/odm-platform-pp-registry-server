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
import org.opendatamesh.platform.pp.registry.utils.client.jackson.PageUtility;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryServiceV1VariablesIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    /*
    Feature: Backward compatibility for legacy Registry variables endpoints

    Background:
        Given a Data Product with id "<productId>" exists
        And the Data Product has version "<version>"
        And the Data Product Version contains variables

    Scenario: Retrieve all variables for a data product version
        When GET /api/v1/pp/registry/products/<productId>/versions/<version>/variables
        Then the response status is 200
        And the response contains a list of RegistryV1VariableResource objects
        And each variable has id, variableName, and variableValue
    */
    @Test
    void whenGetVariablesForDataProductVersionThenReturnListOfVariables() throws IOException {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-v1-variables");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.v1.variables");
        dataProduct.setDisplayName("Test Product V1 Variables");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productId = dataProductResponse.getBody().getUuid();

        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-v1-variables");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dataproduct");
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

        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(versionId);
        variable1.setVariableKey("test-key-1");
        variable1.setVariableValue("test-value-1");

        ResponseEntity<DescriptorVariableRes> variable1Response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(variable1),
                DescriptorVariableRes.class
        );
        assertThat(variable1Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long variable1Id = variable1Response.getBody().getSequenceId();

        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(versionId);
        variable2.setVariableKey("test-key-2");
        variable2.setVariableValue("test-value-2");

        ResponseEntity<DescriptorVariableRes> variable2Response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(variable2),
                DescriptorVariableRes.class
        );
        assertThat(variable2Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long variable2Id = variable2Response.getBody().getSequenceId();

        // When
        ResponseEntity<List<RegistryV1VariableResource>> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/products/" + productId + "/versions/" + version + "/variables"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RegistryV1VariableResource>>() {
                }
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);

        RegistryV1VariableResource foundVariable1 = response.getBody().stream()
                .filter(v -> v.getId().equals(variable1Id))
                .findFirst()
                .orElse(null);
        assertThat(foundVariable1).isNotNull();
        assertThat(foundVariable1.getId()).isEqualTo(variable1Id);
        assertThat(foundVariable1.getVariableName()).isEqualTo("test-key-1");
        assertThat(foundVariable1.getVariableValue()).isEqualTo("test-value-1");

        RegistryV1VariableResource foundVariable2 = response.getBody().stream()
                .filter(v -> v.getId().equals(variable2Id))
                .findFirst()
                .orElse(null);
        assertThat(foundVariable2).isNotNull();
        assertThat(foundVariable2.getId()).isEqualTo(variable2Id);
        assertThat(foundVariable2.getVariableName()).isEqualTo("test-key-2");
        assertThat(foundVariable2.getVariableValue()).isEqualTo("test-value-2");

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable1Id));
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable2Id));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + productId));
    }

    /*
    Scenario: Update variables 
        Given a variable with id "<varId>" exists for the data product version
        When PUT /api/v1/pp/registry/products/<productId>/versions/<version>/variables/<varId>?value=<activityResultValue>
        Then the response status is 200
        And the response contains the updated RegistryV1VariableResource
        And the variable value is updated with the <activityResultValue>
    */
    @Test
    void whenUpdateVariableThenReturnUpdatedVariable() throws IOException {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-v1-update");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.v1.update");
        dataProduct.setDisplayName("Test Product V1 Update");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productId = dataProductResponse.getBody().getUuid();

        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-v1-update");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dataproduct");
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

        DescriptorVariableRes variable = new DescriptorVariableRes();
        variable.setDataProductVersionUuid(versionId);
        variable.setVariableKey("test-key-update");
        variable.setVariableValue("initial-value");

        ResponseEntity<DescriptorVariableRes> variableResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(variable),
                DescriptorVariableRes.class
        );
        assertThat(variableResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long varId = variableResponse.getBody().getSequenceId();

        String activityResultValue = "updated-value";

        // When
        ResponseEntity<RegistryV1VariableResource> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/products/" + productId + "/versions/" + version + "/variables/" + varId + "?value=" + activityResultValue),
                HttpMethod.PUT,
                null,
                RegistryV1VariableResource.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(varId);
        assertThat(response.getBody().getVariableName()).isEqualTo("test-key-update");
        assertThat(response.getBody().getVariableValue()).isEqualTo(activityResultValue);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + varId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + productId));
    }

    /*
    Scenario: Retrieve all variables for a data product version (with auto-creation from descriptor)
    
    Background:
        Given a Data Product with id "<productId>" exists
        And the Data Product has version "<version>"
        And the Data Product Version descriptor contains variable placeholders (e.g., ${apiUrl}, ${databaseName})
        And no variables have been explicitly created for this version
    
    When GET /api/v1/pp/registry/products/<productId>/versions/<version>/variables
    
    Then the response status is 200
    And the response contains a list of RegistryV1VariableResource objects
    And each variable corresponds to a placeholder found in the descriptor
    And variables that didn't exist are automatically created with ${variableKey} as initial value
    And each variable has id, variableName, and variableValue
    
    Note: In the original Registry Service V1, variables were automatically extracted from the descriptor
          and created as empty entries when a DataProductVersion was created. The descriptor was scanned
          for variable placeholders in the format ${variableName} (e.g., ${apiUrl}, ${databaseName}).
          
          To maintain backward compatibility, when retrieving variables via the V1 endpoint, the system
          should:
          1. Parse the DataProductVersion descriptor to identify all variable placeholders
          2. For each placeholder found, check if a corresponding DescriptorVariable exists
          3. If a variable doesn't exist, create it automatically with ${variableKey} as initial value
          4. Return all variables (both existing and newly created) in the response
          
          This ensures that legacy clients expecting variables to be automatically available based on
          the descriptor content will continue to work correctly.
    */
    @Test
    void whenGetVariablesForDataProductVersionWithNoVariablesThenAutoCreateFromDescriptor() throws IOException {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-v1-auto-create");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.v1.auto.create");
        dataProduct.setDisplayName("Test Product V1 Auto Create");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productId = dataProductResponse.getBody().getUuid();

        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-v1-auto-create");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dataproduct");
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

        // Verify no variables exist before the call
        ResponseEntity<PageUtility<DescriptorVariableRes>> variablesBeforeResponse =
                rest.exchange(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES) + "?dataProductVersionUuid=" + versionId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );
        assertThat(variablesBeforeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(variablesBeforeResponse.getBody()).isNotNull();
        assertThat(variablesBeforeResponse.getBody().getContent()).isEmpty();

        // Expected variables from descriptor-with-variables.json (${variableName} format)
        List<String> expectedVariableNames = List.of(
                "apiUrl", "databaseName", "missingVar", "invalidJsonVar", "validKey", "apiUrl1", "apiUrl2"
        );

        // When
        ResponseEntity<List<RegistryV1VariableResource>> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/products/" + productId + "/versions/" + version + "/variables"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).hasSize(expectedVariableNames.size());

        // Verify all expected variables are present
        for (String expectedVariableName : expectedVariableNames) {
            RegistryV1VariableResource foundVariable = response.getBody().stream()
                    .filter(v -> v.getVariableName().equals(expectedVariableName))
                    .findFirst()
                    .orElse(null);
            assertThat(foundVariable)
                    .as("Variable '%s' should be automatically created from descriptor", expectedVariableName)
                    .isNotNull();
            assertThat(foundVariable.getId())
                    .as("Variable '%s' should have an ID", expectedVariableName)
                    .isNotNull();
            assertThat(foundVariable.getVariableName())
                    .as("Variable name should match")
                    .isEqualTo(expectedVariableName);
            assertThat(foundVariable.getVariableValue())
                    .as("Variable '%s' should be initialized with ${%s} as value", expectedVariableName, expectedVariableName)
                    .isEqualTo("${" + expectedVariableName + "}");
        }

        // Verify that variables were actually persisted in the database
        ResponseEntity<PageUtility<DescriptorVariableRes>> variablesAfterResponse = rest.exchange(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES) + "?dataProductVersionUuid=" + versionId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(variablesAfterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(variablesAfterResponse.getBody()).isNotNull();
        assertThat(variablesAfterResponse.getBody().getContent()).hasSize(expectedVariableNames.size());

        // Cleanup
        for (DescriptorVariableRes variable : variablesAfterResponse.getBody().getContent()) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable.getSequenceId()));
        }
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + productId));
    }
}

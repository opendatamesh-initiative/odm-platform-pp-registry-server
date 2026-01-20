package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableResultRes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DescriptorVariableStoreUseCaseControllerIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    // STORE use case
    /*
     *  Scenario: Store descriptor variables successfully
     *
     *  Given a Data Product Version with a descriptor containing variable placeholders (e.g., ${variableName})
     *  And a list of descriptor variables with matching keys and values
     *  When the STORE use case is called with these variables
     *  Then all variables are validated against the descriptor content
     *  And all valid variables are stored in the database
     *  And each variable is associated with the correct Data Product Version
     */
    @Test
    public void whenStoreDescriptorVariablesWithValidKeysThenReturnStoredVariables() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-store-valid");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.store.valid");
        dataProduct.setDisplayName("Test Product for Store Valid");
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
        dataProductVersion.setName("test-version-store-valid");
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

        // Create descriptor variables with keys matching the placeholders in the descriptor
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

        StoreDescriptorVariableCommandRes command = new StoreDescriptorVariableCommandRes();
        command.setDescriptorVariables(variables);

        // When
        ResponseEntity<StoreDescriptorVariableResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(command),
                StoreDescriptorVariableResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull()
                .satisfies(body -> {
                    assertThat(body.getDescriptorVariables()).isNotNull().hasSize(2);
                });

        List<DescriptorVariableRes> storedVariables = response.getBody().getDescriptorVariables();
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(versionId);
            assertThat(v.getVariableKey()).isEqualTo("apiUrl");
            assertThat(v.getVariableValue()).isEqualTo("https://api.example.com");
        });
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(versionId);
            assertThat(v.getVariableKey()).isEqualTo("databaseName");
            assertThat(v.getVariableValue()).isEqualTo("production-db");
        });

        // Verify variables can be retrieved
        for (DescriptorVariableRes storedVariable : storedVariables) {
            ResponseEntity<DescriptorVariableRes> getResponse = rest.getForEntity(
                    apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()),
                    DescriptorVariableRes.class
            );
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
        }

        // Cleanup
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    /*
     *  Scenario: Reject variables with invalid keys
     *
     *  Given a Data Product Version with a descriptor containing specific variable placeholders
     *  And a list of descriptor variables where some keys do not match any placeholder in the descriptor
     *  When the STORE use case is called with these variables
     *  Then a BadRequestException is thrown with details about invalid variable keys
     *  And no variables are stored in the database (transactional rollback)
     */
    @Test
    public void whenStoreDescriptorVariablesWithInvalidKeysThenReturnBadRequest() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-store-invalid");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.store.invalid");
        dataProduct.setDisplayName("Test Product for Store Invalid");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create data product version with ODM spec and descriptor containing only specific variable placeholders
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-store-invalid");
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

        // Create descriptor variables where one key is valid and one is invalid
        List<DescriptorVariableRes> variables = new ArrayList<>();
        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(versionId);
        variable1.setVariableKey("validKey");
        variable1.setVariableValue("valid-value");
        variables.add(variable1);

        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(versionId);
        variable2.setVariableKey("invalidKey");
        variable2.setVariableValue("invalid-value");
        variables.add(variable2);

        StoreDescriptorVariableCommandRes command = new StoreDescriptorVariableCommandRes();
        command.setDescriptorVariables(variables);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(command),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("invalidKey");
        assertThat(response.getBody()).contains("not found in descriptor");

        // Verify no variables were stored
        ResponseEntity<String> searchResponse = rest.getForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES) + "?dataProductVersionUuid=" + versionId,
                String.class
        );
        // Should return empty page or not contain the variables we tried to store
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    /*
     *  Scenario: Store variables across multiple Data Product Versions
     *
     *  Given two different Data Product Versions, each with descriptors containing variable placeholders
     *  And a list of descriptor variables where some belong to the first DPV and some to the second DPV
     *  And all variable keys are valid for their respective descriptors
     *  When the STORE use case is called with this mixed list of variables
     *  Then variables are grouped by Data Product Version UUID
     *  And each group is processed in a separate transaction
     *  And all variables are stored successfully
     *  And each variable is correctly associated with its Data Product Version
     */
    @Test
    public void whenStoreDescriptorVariablesForMultipleDataProductVersionsThenReturnStoredVariables() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-store-multi");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.store.multi");
        dataProduct.setDisplayName("Test Product for Store Multi");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create first data product version with ODM spec
        DataProductVersionRes dataProductVersion1 = new DataProductVersionRes();
        dataProductVersion1.setName("test-version-store-multi-1");
        dataProductVersion1.setDescription("Test version 1 description");
        dataProductVersion1.setTag("v1.0.0");
        dataProductVersion1.setVersionNumber("1.0.0");
        dataProductVersion1.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion1.setDataProduct(dataProductResponse.getBody());
        dataProductVersion1.setSpec("DPDS");
        dataProductVersion1.setSpecVersion("1.0.0");
        JsonNode content1 = loadDescriptor("descriptor-with-variables.json");
        dataProductVersion1.setContent(content1);

        ResponseEntity<DataProductVersionRes> version1Response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion1),
                DataProductVersionRes.class
        );
        assertThat(version1Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String version1Id = version1Response.getBody().getUuid();

        // Create second data product version with ODM spec
        DataProductVersionRes dataProductVersion2 = new DataProductVersionRes();
        dataProductVersion2.setName("test-version-store-multi-2");
        dataProductVersion2.setDescription("Test version 2 description");
        dataProductVersion2.setTag("v1.0.1");
        dataProductVersion2.setVersionNumber("1.0.1");
        dataProductVersion2.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion2.setDataProduct(dataProductResponse.getBody());
        dataProductVersion2.setSpec("DPDS");
        dataProductVersion2.setSpecVersion("1.0.0");
        JsonNode content2 = loadDescriptor("descriptor-with-variables.json");
        dataProductVersion2.setContent(content2);

        ResponseEntity<DataProductVersionRes> version2Response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion2),
                DataProductVersionRes.class
        );
        assertThat(version2Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String version2Id = version2Response.getBody().getUuid();

        // Create descriptor variables for both versions
        List<DescriptorVariableRes> variables = new ArrayList<>();

        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(version1Id);
        variable1.setVariableKey("apiUrl1");
        variable1.setVariableValue("https://api1.example.com");
        variables.add(variable1);

        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(version2Id);
        variable2.setVariableKey("apiUrl2");
        variable2.setVariableValue("https://api2.example.com");
        variables.add(variable2);

        StoreDescriptorVariableCommandRes command = new StoreDescriptorVariableCommandRes();
        command.setDescriptorVariables(variables);

        // When
        ResponseEntity<StoreDescriptorVariableResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(command),
                StoreDescriptorVariableResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull()
                .satisfies(body -> {
                    assertThat(body.getDescriptorVariables()).isNotNull().hasSize(2);
                });

        List<DescriptorVariableRes> storedVariables = response.getBody().getDescriptorVariables();
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(version1Id);
            assertThat(v.getVariableKey()).isEqualTo("apiUrl1");
            assertThat(v.getVariableValue()).isEqualTo("https://api1.example.com");
        });
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(version2Id);
            assertThat(v.getVariableKey()).isEqualTo("apiUrl2");
            assertThat(v.getVariableValue()).isEqualTo("https://api2.example.com");
        });

        // Verify variables can be retrieved and are associated with correct versions
        for (DescriptorVariableRes storedVariable : storedVariables) {
            ResponseEntity<DescriptorVariableRes> getResponse = rest.getForEntity(
                    apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()),
                    DescriptorVariableRes.class
            );
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
            if (getResponse.getBody().getVariableKey().equals("apiUrl1")) {
                assertThat(getResponse.getBody().getDataProductVersionUuid()).isEqualTo(version1Id);
            } else if (getResponse.getBody().getVariableKey().equals("apiUrl2")) {
                assertThat(getResponse.getBody().getDataProductVersionUuid()).isEqualTo(version2Id);
            }
        }

        // Cleanup
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version1Id));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version2Id));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    /*
     *  Scenario: Store descriptor variables successfully
     *
     *  Given a Data Product Version with a descriptor of an unsupported specification
     *  And a list of descriptor variables
     *  When the STORE use case is called with these variables
     *  Then all the variables are stored successfully
     */
    @Test
    public void whenStoreDescriptorVariablesForUnsupportedSpecThenReturnStoredVariables() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-store-unsupported");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.store.unsupported");
        dataProduct.setDisplayName("Test Product for Store Unsupported Spec");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create data product version with UNSUPPORTED spec and descriptor
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-store-unsupported");
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

        // Create descriptor variables (any keys/values since validation is skipped for unsupported specs)
        List<DescriptorVariableRes> variables = new ArrayList<>();
        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(versionId);
        variable1.setVariableKey("anyKey1");
        variable1.setVariableValue("anyValue1");
        variables.add(variable1);

        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(versionId);
        variable2.setVariableKey("anyKey2");
        variable2.setVariableValue("anyValue2");
        variables.add(variable2);

        StoreDescriptorVariableCommandRes command = new StoreDescriptorVariableCommandRes();
        command.setDescriptorVariables(variables);

        // When
        ResponseEntity<StoreDescriptorVariableResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(command),
                StoreDescriptorVariableResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull()
                .satisfies(body -> {
                    assertThat(body.getDescriptorVariables()).isNotNull().hasSize(2);
                });

        List<DescriptorVariableRes> storedVariables = response.getBody().getDescriptorVariables();
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(versionId);
            assertThat(v.getVariableKey()).isEqualTo("anyKey1");
            assertThat(v.getVariableValue()).isEqualTo("anyValue1");
        });
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(versionId);
            assertThat(v.getVariableKey()).isEqualTo("anyKey2");
            assertThat(v.getVariableValue()).isEqualTo("anyValue2");
        });

        // Verify variables can be retrieved
        for (DescriptorVariableRes storedVariable : storedVariables) {
            ResponseEntity<DescriptorVariableRes> getResponse = rest.getForEntity(
                    apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()),
                    DescriptorVariableRes.class
            );
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
        }

        // Cleanup
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    /*
     *  Scenario: Store variables with same keys overrides existing values
     *
     *  Given a Data Product Version with a descriptor containing variable placeholders
     *  And some variables have already been stored for that version
     *  When the STORE use case is called with new variables using the same keys but different values
     *  Then the old variable values are overridden with the new ones
     *  And the stored variables reflect the updated values
     */
    @Test
    public void whenStoreDescriptorVariablesWithSameKeysThenOverrideExistingValues() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-store-override");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.store.override");
        dataProduct.setDisplayName("Test Product for Store Override");
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
        dataProductVersion.setName("test-version-store-override");
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

        // First, store initial variables
        List<DescriptorVariableRes> initialVariables = new ArrayList<>();
        DescriptorVariableRes initialVariable1 = new DescriptorVariableRes();
        initialVariable1.setDataProductVersionUuid(versionId);
        initialVariable1.setVariableKey("apiUrl");
        initialVariable1.setVariableValue("https://old-api.example.com");
        initialVariables.add(initialVariable1);

        DescriptorVariableRes initialVariable2 = new DescriptorVariableRes();
        initialVariable2.setDataProductVersionUuid(versionId);
        initialVariable2.setVariableKey("databaseName");
        initialVariable2.setVariableValue("old-db");
        initialVariables.add(initialVariable2);

        StoreDescriptorVariableCommandRes initialCommand = new StoreDescriptorVariableCommandRes();
        initialCommand.setDescriptorVariables(initialVariables);

        ResponseEntity<StoreDescriptorVariableResultRes> initialResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(initialCommand),
                StoreDescriptorVariableResultRes.class
        );
        assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Store new variables with same keys but different values
        List<DescriptorVariableRes> newVariables = new ArrayList<>();
        DescriptorVariableRes newVariable1 = new DescriptorVariableRes();
        newVariable1.setDataProductVersionUuid(versionId);
        newVariable1.setVariableKey("apiUrl");
        newVariable1.setVariableValue("https://new-api.example.com");
        newVariables.add(newVariable1);

        DescriptorVariableRes newVariable2 = new DescriptorVariableRes();
        newVariable2.setDataProductVersionUuid(versionId);
        newVariable2.setVariableKey("databaseName");
        newVariable2.setVariableValue("new-db");
        newVariables.add(newVariable2);

        StoreDescriptorVariableCommandRes newCommand = new StoreDescriptorVariableCommandRes();
        newCommand.setDescriptorVariables(newVariables);

        // When
        ResponseEntity<StoreDescriptorVariableResultRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/store"),
                new HttpEntity<>(newCommand),
                StoreDescriptorVariableResultRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull()
                .satisfies(body -> {
                    assertThat(body.getDescriptorVariables()).isNotNull().hasSize(2);
                });

        List<DescriptorVariableRes> storedVariables = response.getBody().getDescriptorVariables();
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(versionId);
            assertThat(v.getVariableKey()).isEqualTo("apiUrl");
            assertThat(v.getVariableValue()).isEqualTo("https://new-api.example.com");
        });
        assertThat(storedVariables).anySatisfy(v -> {
            assertThat(v.getSequenceId()).isNotNull();
            assertThat(v.getDataProductVersionUuid()).isEqualTo(versionId);
            assertThat(v.getVariableKey()).isEqualTo("databaseName");
            assertThat(v.getVariableValue()).isEqualTo("new-db");
        });

        // Verify that the old values are no longer present by searching for all variables
        ResponseEntity<String> searchResponse = rest.getForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES) + "?dataProductVersionUuid=" + versionId,
                String.class
        );
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).doesNotContain("https://old-api.example.com");
        assertThat(searchResponse.getBody()).doesNotContain("old-db");
        assertThat(searchResponse.getBody()).contains("https://new-api.example.com");
        assertThat(searchResponse.getBody()).contains("new-db");

        // Cleanup
        for (DescriptorVariableRes storedVariable : storedVariables) {
            rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + storedVariable.getSequenceId()));
        }
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }


}

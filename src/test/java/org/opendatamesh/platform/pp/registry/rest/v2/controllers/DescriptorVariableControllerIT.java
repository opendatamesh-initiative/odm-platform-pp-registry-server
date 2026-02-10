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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DescriptorVariableControllerIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void whenCreateDescriptorVariableThenReturnCreatedDescriptorVariable() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-variable");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.variable");
        dataProduct.setDisplayName("Test Product for Variable");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();


        // Create data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String versionId = versionResponse.getBody().getUuid();


        // Create descriptor variable
        DescriptorVariableRes descriptorVariable = new DescriptorVariableRes();
        descriptorVariable.setDataProductVersionUuid(versionId);
        descriptorVariable.setVariableKey("test-key");
        descriptorVariable.setVariableValue("test-value");

        // When
        ResponseEntity<DescriptorVariableRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(descriptorVariable),
                DescriptorVariableRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSequenceId()).isNotNull();
        assertThat(response.getBody().getDataProductVersionUuid()).isEqualTo(versionId);
        assertThat(response.getBody().getVariableKey()).isEqualTo("test-key");
        assertThat(response.getBody().getVariableValue()).isEqualTo("test-value");

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + response.getBody().getSequenceId()));

        // Cleanup version
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));

        // Cleanup data product
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));

    }

    @Test
    public void whenGetDescriptorVariableByIdThenReturnDescriptorVariable() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-get");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.get");
        dataProduct.setDisplayName("Test Product for Get");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        try {
            // Create data product version
            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-get");
            dataProductVersion.setDescription("Test version description");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());
            dataProductVersion.setSpec("dpds");
            dataProductVersion.setSpecVersion("1.0.0");
            JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-get\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
            dataProductVersion.setContent(content);

            ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = versionResponse.getBody().getUuid();

            try {
                // Create descriptor variable
                DescriptorVariableRes descriptorVariable = new DescriptorVariableRes();
                descriptorVariable.setDataProductVersionUuid(versionId);
                descriptorVariable.setVariableKey("test-key-get");
                descriptorVariable.setVariableValue("test-value-get");

                ResponseEntity<DescriptorVariableRes> createResponse = rest.postForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        new HttpEntity<>(descriptorVariable),
                        DescriptorVariableRes.class
                );
                assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Long variableId = createResponse.getBody().getSequenceId();

                // When
                ResponseEntity<DescriptorVariableRes> response = rest.getForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variableId),
                        DescriptorVariableRes.class
                );

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getSequenceId()).isEqualTo(variableId);
                assertThat(response.getBody().getDataProductVersionUuid()).isEqualTo(versionId);
                assertThat(response.getBody().getVariableKey()).isEqualTo("test-key-get");
                assertThat(response.getBody().getVariableValue()).isEqualTo("test-value-get");

                // Cleanup
                rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variableId));
            } finally {
                // Cleanup version
                rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
            }
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenGetDescriptorVariableWithNonExistentIdThenReturnNotFound() {
        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/999999"),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenSearchDescriptorVariablesThenReturnDescriptorVariablesList() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-search");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.search");
        dataProduct.setDisplayName("Test Product for Search");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        try {
            // Create data product version
            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-search");
            dataProductVersion.setDescription("Test version description");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());
            dataProductVersion.setSpec("dpds");
            dataProductVersion.setSpecVersion("1.0.0");
            JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-search\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
            dataProductVersion.setContent(content);

            ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = versionResponse.getBody().getUuid();

            try {
                // Create first descriptor variable
                DescriptorVariableRes variable1 = new DescriptorVariableRes();
                variable1.setDataProductVersionUuid(versionId);
                variable1.setVariableKey("key1");
                variable1.setVariableValue("value1");

                ResponseEntity<DescriptorVariableRes> createResponse1 = rest.postForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        new HttpEntity<>(variable1),
                        DescriptorVariableRes.class
                );
                assertThat(createResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Long variable1Id = createResponse1.getBody().getSequenceId();

                // Create second descriptor variable
                DescriptorVariableRes variable2 = new DescriptorVariableRes();
                variable2.setDataProductVersionUuid(versionId);
                variable2.setVariableKey("key2");
                variable2.setVariableValue("value2");

                ResponseEntity<DescriptorVariableRes> createResponse2 = rest.postForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        new HttpEntity<>(variable2),
                        DescriptorVariableRes.class
                );
                assertThat(createResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Long variable2Id = createResponse2.getBody().getSequenceId();

                // When
                ResponseEntity<JsonNode> response = rest.exchange(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        HttpMethod.GET,
                        null,
                        JsonNode.class
                );

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                JsonNode responseBody = response.getBody();
                assertThat(responseBody.has("content")).isTrue();
                assertThat(responseBody.has("totalElements")).isTrue();

                // Cleanup
                rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable1Id));
                rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable2Id));
            } finally {
                // Cleanup version
                rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
            }
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenSearchDescriptorVariablesWithFilterThenReturnFilteredResults() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-filter");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.filter");
        dataProduct.setDisplayName("Test Product for Filter");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        try {
            // Create first data product version
            DataProductVersionRes version1 = new DataProductVersionRes();
            version1.setName("test-version-filter-1");
            version1.setDescription("Test version description 1");
            version1.setTag("v1.0.0");
            version1.setVersionNumber("1.0.0");
            version1.setValidationState(DataProductVersionValidationStateRes.PENDING);
            version1.setDataProduct(dataProductResponse.getBody());
            version1.setSpec("dpds");
            version1.setSpecVersion("1.0.0");
            JsonNode content1 = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-filter-1\",\"version\":\"1.0.0\",\"description\":\"Test version description 1\"}}");
            version1.setContent(content1);

            ResponseEntity<DataProductVersionRes> version1Response = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(version1),
                    DataProductVersionRes.class
            );
            assertThat(version1Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String version1Id = version1Response.getBody().getUuid();

            // Create second data product version
            DataProductVersionRes version2 = new DataProductVersionRes();
            version2.setName("test-version-filter-2");
            version2.setDescription("Test version description 2");
            version2.setTag("v1.0.1");
            version2.setVersionNumber("1.0.1");
            version2.setValidationState(DataProductVersionValidationStateRes.PENDING);
            version2.setDataProduct(dataProductResponse.getBody());
            version2.setSpec("dpds");
            version2.setSpecVersion("1.0.0");
            JsonNode content2 = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-filter-2\",\"version\":\"1.0.1\",\"description\":\"Test version description 2\"}}");
            version2.setContent(content2);

            ResponseEntity<DataProductVersionRes> version2Response = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(version2),
                    DataProductVersionRes.class
            );
            assertThat(version2Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String version2Id = version2Response.getBody().getUuid();

            try {
                // Create descriptor variable for version 1
                DescriptorVariableRes variable1 = new DescriptorVariableRes();
                variable1.setDataProductVersionUuid(version1Id);
                variable1.setVariableKey("filter-key-1");
                variable1.setVariableValue("filter-value-1");

                ResponseEntity<DescriptorVariableRes> createResponse1 = rest.postForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        new HttpEntity<>(variable1),
                        DescriptorVariableRes.class
                );
                assertThat(createResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Long variable1Id = createResponse1.getBody().getSequenceId();

                // Create descriptor variable for version 2
                DescriptorVariableRes variable2 = new DescriptorVariableRes();
                variable2.setDataProductVersionUuid(version2Id);
                variable2.setVariableKey("filter-key-2");
                variable2.setVariableValue("filter-value-2");

                ResponseEntity<DescriptorVariableRes> createResponse2 = rest.postForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        new HttpEntity<>(variable2),
                        DescriptorVariableRes.class
                );
                assertThat(createResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Long variable2Id = createResponse2.getBody().getSequenceId();

                // When - Search with filter for version 1
                ResponseEntity<JsonNode> response = rest.exchange(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES) + "?dataProductVersionUuid=" + version1Id,
                        HttpMethod.GET,
                        null,
                        JsonNode.class
                );

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                JsonNode responseBody = response.getBody();
                assertThat(responseBody.has("content")).isTrue();
                assertThat(responseBody.has("totalElements")).isTrue();
                assertThat(responseBody.get("totalElements").asInt()).isEqualTo(1);

                // Cleanup
                rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable1Id));
                rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable2Id));
            } finally {
                // Cleanup versions
                rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version1Id));
                rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version2Id));
            }
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenUpdateDescriptorVariableThenReturnUpdatedDescriptorVariable() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-update");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.update");
        dataProduct.setDisplayName("Test Product for Update");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        try {
            // Create data product version
            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-update");
            dataProductVersion.setDescription("Test version description");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());
            dataProductVersion.setSpec("dpds");
            dataProductVersion.setSpecVersion("1.0.0");
            JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-update\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
            dataProductVersion.setContent(content);

            ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = versionResponse.getBody().getUuid();

            try {
                // Create initial descriptor variable
                DescriptorVariableRes initialVariable = new DescriptorVariableRes();
                initialVariable.setDataProductVersionUuid(versionId);
                initialVariable.setVariableKey("initial-key");
                initialVariable.setVariableValue("initial-value");

                ResponseEntity<DescriptorVariableRes> createResponse = rest.postForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        new HttpEntity<>(initialVariable),
                        DescriptorVariableRes.class
                );
                assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Long variableId = createResponse.getBody().getSequenceId();

                // Create updated descriptor variable
                DescriptorVariableRes updatedVariable = new DescriptorVariableRes();
                updatedVariable.setSequenceId(variableId);
                updatedVariable.setDataProductVersionUuid(versionId);
                updatedVariable.setVariableKey("updated-key");
                updatedVariable.setVariableValue("updated-value");

                // When
                ResponseEntity<DescriptorVariableRes> response = rest.exchange(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variableId),
                        HttpMethod.PUT,
                        new HttpEntity<>(updatedVariable),
                        DescriptorVariableRes.class
                );

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getSequenceId()).isEqualTo(variableId);
                assertThat(response.getBody().getVariableKey()).isEqualTo("updated-key");
                assertThat(response.getBody().getVariableValue()).isEqualTo("updated-value");

                // Cleanup
                rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variableId));
            } finally {
                // Cleanup version
                rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
            }
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenDeleteDescriptorVariableThenReturnNoContentAndDescriptorVariableIsDeleted() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-delete");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.delete");
        dataProduct.setDisplayName("Test Product for Delete");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        try {
            // Create data product version
            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-delete");
            dataProductVersion.setDescription("Test version description");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());
            dataProductVersion.setSpec("dpds");
            dataProductVersion.setSpecVersion("1.0.0");
            JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-delete\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
            dataProductVersion.setContent(content);

            ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = versionResponse.getBody().getUuid();

            try {
                // Create descriptor variable
                DescriptorVariableRes descriptorVariable = new DescriptorVariableRes();
                descriptorVariable.setDataProductVersionUuid(versionId);
                descriptorVariable.setVariableKey("delete-key");
                descriptorVariable.setVariableValue("delete-value");

                ResponseEntity<DescriptorVariableRes> createResponse = rest.postForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                        new HttpEntity<>(descriptorVariable),
                        DescriptorVariableRes.class
                );
                assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Long variableId = createResponse.getBody().getSequenceId();

                // When
                ResponseEntity<Void> response = rest.exchange(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variableId),
                        HttpMethod.DELETE,
                        null,
                        Void.class
                );

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

                // Verify deletion
                ResponseEntity<String> getResponse = rest.getForEntity(
                        apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variableId),
                        String.class
                );
                assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            } finally {
                // Cleanup version
                rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
            }
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenCreateDescriptorVariableWithInvalidDataThenReturnBadRequest() {
        // Given
        DescriptorVariableRes invalidVariable = new DescriptorVariableRes();
        // Missing required fields: dataProductVersionUuid and variableKey

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(invalidVariable),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenCreateDescriptorVariableWithNonExistentDataProductVersionThenReturnNotFound() {
        // Given
        DescriptorVariableRes descriptorVariable = new DescriptorVariableRes();
        descriptorVariable.setDataProductVersionUuid("non-existent-version-id");
        descriptorVariable.setVariableKey("test-key");
        descriptorVariable.setVariableValue("test-value");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(descriptorVariable),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenCreateDescriptorVariableWithDuplicateKeyForSameDataProductVersionThenReturnConflict() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-duplicate-key");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.duplicate.key");
        dataProduct.setDisplayName("Test Product for Duplicate Key");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-duplicate-key");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-duplicate-key\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String versionId = versionResponse.getBody().getUuid();

        // Create first descriptor variable
        DescriptorVariableRes firstVariable = new DescriptorVariableRes();
        firstVariable.setDataProductVersionUuid(versionId);
        firstVariable.setVariableKey("duplicate-key");
        firstVariable.setVariableValue("first-value");

        ResponseEntity<DescriptorVariableRes> createFirstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(firstVariable),
                DescriptorVariableRes.class
        );
        assertThat(createFirstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long firstVariableId = createFirstResponse.getBody().getSequenceId();

        // Create second descriptor variable with the same key for the same version
        DescriptorVariableRes duplicateVariable = new DescriptorVariableRes();
        duplicateVariable.setDataProductVersionUuid(versionId);
        duplicateVariable.setVariableKey("duplicate-key");
        duplicateVariable.setVariableValue("second-value");

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(duplicateVariable),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + firstVariableId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenCreateDescriptorVariableWithSameKeyForDifferentDataProductVersionsThenReturnCreated() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-same-key-different-versions");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.same.key.different.versions");
        dataProduct.setDisplayName("Test Product for Same Key Different Versions");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create first data product version
        DataProductVersionRes version1 = new DataProductVersionRes();
        version1.setName("test-version-1");
        version1.setDescription("Test version 1 description");
        version1.setTag("v1.0.0");
        version1.setVersionNumber("1.0.0");
        version1.setValidationState(DataProductVersionValidationStateRes.PENDING);
        version1.setDataProduct(dataProductResponse.getBody());
        version1.setSpec("dpds");
        version1.setSpecVersion("1.0.0");
        JsonNode content1 = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-1\",\"version\":\"1.0.0\",\"description\":\"Test version 1 description\"}}");
        version1.setContent(content1);

        ResponseEntity<DataProductVersionRes> version1Response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(version1),
                DataProductVersionRes.class
        );
        assertThat(version1Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String version1Id = version1Response.getBody().getUuid();

        // Create second data product version
        DataProductVersionRes version2 = new DataProductVersionRes();
        version2.setName("test-version-2");
        version2.setDescription("Test version 2 description");
        version2.setTag("v1.0.1");
        version2.setVersionNumber("1.0.1");
        version2.setValidationState(DataProductVersionValidationStateRes.PENDING);
        version2.setDataProduct(dataProductResponse.getBody());
        version2.setSpec("dpds");
        version2.setSpecVersion("1.0.0");
        JsonNode content2 = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-2\",\"version\":\"1.0.1\",\"description\":\"Test version 2 description\"}}");
        version2.setContent(content2);

        ResponseEntity<DataProductVersionRes> version2Response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(version2),
                DataProductVersionRes.class
        );
        assertThat(version2Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String version2Id = version2Response.getBody().getUuid();

        // Create descriptor variable for version 1
        DescriptorVariableRes variable1 = new DescriptorVariableRes();
        variable1.setDataProductVersionUuid(version1Id);
        variable1.setVariableKey("same-key");
        variable1.setVariableValue("value-1");

        ResponseEntity<DescriptorVariableRes> createResponse1 = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(variable1),
                DescriptorVariableRes.class
        );
        assertThat(createResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long variable1Id = createResponse1.getBody().getSequenceId();

        // Create descriptor variable for version 2 with the same key (should succeed)
        DescriptorVariableRes variable2 = new DescriptorVariableRes();
        variable2.setDataProductVersionUuid(version2Id);
        variable2.setVariableKey("same-key");
        variable2.setVariableValue("value-2");

        // When
        ResponseEntity<DescriptorVariableRes> createResponse2 = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(variable2),
                DescriptorVariableRes.class
        );

        // Then
        assertThat(createResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse2.getBody()).isNotNull();
        assertThat(createResponse2.getBody().getVariableKey()).isEqualTo("same-key");
        assertThat(createResponse2.getBody().getDataProductVersionUuid()).isEqualTo(version2Id);
        Long variable2Id = createResponse2.getBody().getSequenceId();

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable1Id));
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + variable2Id));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version1Id));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + version2Id));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenUpdateDescriptorVariableWithDuplicateKeyForSameDataProductVersionThenReturnConflict() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-update-duplicate-key");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.update.duplicate.key");
        dataProduct.setDisplayName("Test Product for Update Duplicate Key");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create data product version
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("test-version-update-duplicate-key");
        dataProductVersion.setDescription("Test version description");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setDataProduct(dataProductResponse.getBody());
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-update-duplicate-key\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
        dataProductVersion.setContent(content);

        ResponseEntity<DataProductVersionRes> versionResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(dataProductVersion),
                DataProductVersionRes.class
        );
        assertThat(versionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String versionId = versionResponse.getBody().getUuid();

        // Create first descriptor variable
        DescriptorVariableRes firstVariable = new DescriptorVariableRes();
        firstVariable.setDataProductVersionUuid(versionId);
        firstVariable.setVariableKey("existing-key");
        firstVariable.setVariableValue("first-value");

        ResponseEntity<DescriptorVariableRes> createFirstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(firstVariable),
                DescriptorVariableRes.class
        );
        assertThat(createFirstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long firstVariableId = createFirstResponse.getBody().getSequenceId();

        // Create second descriptor variable with different key
        DescriptorVariableRes secondVariable = new DescriptorVariableRes();
        secondVariable.setDataProductVersionUuid(versionId);
        secondVariable.setVariableKey("different-key");
        secondVariable.setVariableValue("second-value");

        ResponseEntity<DescriptorVariableRes> createSecondResponse = rest.postForEntity(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES),
                new HttpEntity<>(secondVariable),
                DescriptorVariableRes.class
        );
        assertThat(createSecondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long secondVariableId = createSecondResponse.getBody().getSequenceId();

        // Try to update second variable to have the same key as first variable
        DescriptorVariableRes updatedVariable = new DescriptorVariableRes();
        updatedVariable.setSequenceId(secondVariableId);
        updatedVariable.setDataProductVersionUuid(versionId);
        updatedVariable.setVariableKey("existing-key"); // Same key as first variable
        updatedVariable.setVariableValue("updated-value");

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + secondVariableId),
                HttpMethod.PUT,
                new HttpEntity<>(updatedVariable),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + firstVariableId));
        rest.delete(apiUrl(RoutesV2.DESCRIPTOR_VARIABLES, "/" + secondVariableId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }
}

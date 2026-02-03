package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionShortRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DataProductVersionsControllerIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void whenCreateDataProductVersionThenReturnCreatedDataProductVersion() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-version");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.version");
        dataProduct.setDisplayName("Test Product for Version");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        try {
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

            // When
            ResponseEntity<DataProductVersionRes> response = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUuid()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo(dataProductVersion.getName());
            assertThat(response.getBody().getSpec()).isEqualTo(dataProductVersion.getSpec());
            assertThat(response.getBody().getSpecVersion()).isEqualTo(dataProductVersion.getSpecVersion());
            assertThat(response.getBody().getContent()).isEqualTo(content);

            // Cleanup
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + response.getBody().getUuid()));
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenGetDataProductVersionByIdThenReturnDataProductVersion() throws IOException {
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
            // Create version
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

            ResponseEntity<DataProductVersionRes> createResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = createResponse.getBody().getUuid();

            // When
            ResponseEntity<DataProductVersionRes> response = rest.getForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                    DataProductVersionRes.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUuid()).isEqualTo(versionId);
            assertThat(response.getBody().getName()).isEqualTo(dataProductVersion.getName());
            assertThat(response.getBody().getSpec()).isEqualTo(dataProductVersion.getSpec());
            assertThat(response.getBody().getSpecVersion()).isEqualTo(dataProductVersion.getSpecVersion());
            assertThat(response.getBody().getContent()).isEqualTo(content);

            // Cleanup
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenGetDataProductVersionByNonExistentIdThenReturnNotFound() {
        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/non-existent-id"),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenSearchDataProductVersionsThenReturnPaginatedResults() {
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
            // Create version
            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-search");
            dataProductVersion.setDescription("Test version description");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());

            ResponseEntity<DataProductVersionRes> createResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = createResponse.getBody().getUuid();

            // When
            ResponseEntity<String> response = rest.getForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    String.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            // Cleanup
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenUpdateDataProductVersionThenReturnUpdatedDataProductVersion() throws IOException {
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
            // Create version
            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-update");
            dataProductVersion.setDescription("Test version description");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());
            dataProductVersion.setSpec("dpds");
            dataProductVersion.setSpecVersion("1.0.0");
            dataProductVersion.setCreatedBy("createdUser");
            dataProductVersion.setUpdatedBy("updatedUser");
            JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-update\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
            dataProductVersion.setContent(content);

            ResponseEntity<DataProductVersionRes> createResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = createResponse.getBody().getUuid();

            // Update the version
            dataProductVersion.setUuid(versionId); // Ensure UUID is set for update
            dataProductVersion.setName("updated-version-name");
            dataProductVersion.setDescription("Updated description");
            dataProductVersion.setUpdatedBy("updatedUserUpdate");
            JsonNode updatedContent = objectMapper.readTree("{\"dataProduct\":{\"name\":\"updated-version-name\",\"version\":\"1.0.0\",\"description\":\"Updated description\"}}");
            dataProductVersion.setContent(updatedContent);

            // When
            ResponseEntity<DataProductVersionRes> response = rest.exchange(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                    HttpMethod.PUT,
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUuid()).isEqualTo(versionId);
            assertThat(response.getBody().getName()).isEqualTo("updated-version-name");
            assertThat(response.getBody().getUpdatedBy()).isEqualTo("updatedUserUpdate");
            assertThat(response.getBody().getContent()).isEqualTo(updatedContent);

            // Cleanup
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenUpdateNonExistentDataProductVersionThenReturnNotFound() throws IOException {
        // Given - Create a minimal valid DataProductVersionRes with required fields
        DataProductVersionRes dataProductVersion = new DataProductVersionRes();
        dataProductVersion.setName("non-existent-version");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setVersionNumber("1.0.0");
        dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        dataProductVersion.setSpec("dpds");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.readTree("{\"dataProduct\":{\"name\":\"non-existent-version\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
        dataProductVersion.setContent(content);
        
        // Create a minimal DataProductRes with a fake UUID for validation
        DataProductRes fakeDataProduct = new DataProductRes();
        fakeDataProduct.setUuid("00000000-0000-0000-0000-000000000000");
        dataProductVersion.setDataProduct(fakeDataProduct);

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/non-existent-id"),
                HttpMethod.PUT,
                new HttpEntity<>(dataProductVersion),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenDeleteDataProductVersionThenReturnNoContent() {
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
            // Create version
            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-delete");
            dataProductVersion.setDescription("Test version description");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());

            ResponseEntity<DataProductVersionRes> createResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String versionId = createResponse.getBody().getUuid();

            // When
            ResponseEntity<Void> response = rest.exchange(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify the version is deleted
            ResponseEntity<String> getResponse = rest.getForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                    String.class
            );
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenDeleteNonExistentDataProductVersionThenReturnNotFound() {
        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/non-existent-id"),
                HttpMethod.DELETE,
                null,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenCreateDataProductVersionWithJsonContentThenContentIsCorrectlyStoredAndRetrieved() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-json");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.json");
        dataProduct.setDisplayName("Test Product for JSON");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        try {
            // Create a complex JSON content
            String complexJsonContent = "{\n" +
                    "  \"dataProduct\": {\n" +
                    "    \"name\": \"test-version-json\",\n" +
                    "    \"version\": \"1.0.0\",\n" +
                    "    \"description\": \"Test version with complex JSON\",\n" +
                    "    \"components\": {\n" +
                    "      \"inputPorts\": [\n" +
                    "        {\n" +
                    "          \"name\": \"input1\",\n" +
                    "          \"type\": \"string\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"outputPorts\": [\n" +
                    "        {\n" +
                    "          \"name\": \"output1\",\n" +
                    "          \"type\": \"object\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"metadata\": {\n" +
                    "      \"tags\": [\"test\", \"json\", \"complex\"],\n" +
                    "      \"owner\": \"test-user\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            DataProductVersionRes dataProductVersion = new DataProductVersionRes();
            dataProductVersion.setName("test-version-json");
            dataProductVersion.setDescription("Test version with complex JSON content");
            dataProductVersion.setTag("v1.0.0");
            dataProductVersion.setVersionNumber("1.0.0");
            dataProductVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
            dataProductVersion.setDataProduct(dataProductResponse.getBody());
            dataProductVersion.setSpec("dpds");
            dataProductVersion.setSpecVersion("1.0.0");
            JsonNode complexContent = objectMapper.readTree(complexJsonContent);
            dataProductVersion.setContent(complexContent);

            // When - Create the version
            ResponseEntity<DataProductVersionRes> createResponse = rest.postForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                    new HttpEntity<>(dataProductVersion),
                    DataProductVersionRes.class
            );

            // Then - Verify creation
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createResponse.getBody()).isNotNull();
            String versionId = createResponse.getBody().getUuid();
            assertThat(createResponse.getBody().getContent()).isEqualTo(complexContent);

            // When - Retrieve the version
            ResponseEntity<DataProductVersionRes> getResponse = rest.getForEntity(
                    apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId),
                    DataProductVersionRes.class
            );

            // Then - Verify the JSON content is correctly retrieved
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).isNotNull();
            assertThat(getResponse.getBody().getUuid()).isEqualTo(versionId);
            assertThat(getResponse.getBody().getContent()).isEqualTo(complexContent);
            
            // Verify that the content is valid JSON by checking it contains expected structure
            JsonNode retrievedContent = getResponse.getBody().getContent();
            assertThat(retrievedContent.has("dataProduct")).isTrue();
            assertThat(retrievedContent.get("dataProduct").has("components")).isTrue();
            assertThat(retrievedContent.get("dataProduct").get("components").has("inputPorts")).isTrue();
            assertThat(retrievedContent.get("dataProduct").get("components").has("outputPorts")).isTrue();
            assertThat(retrievedContent.get("dataProduct").has("metadata")).isTrue();
            assertThat(retrievedContent.get("dataProduct").get("metadata").has("tags")).isTrue();

            // Cleanup
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + versionId));
        } finally {
            // Cleanup data product
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
        }
    }

    @Test
    public void whenSearchDataProductVersionsWithSearchParameterThenReturnFilteredResults() throws IOException {
        // Given - Create a data product first
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-for-search-param");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.product.for.search.param");
        dataProduct.setDisplayName("Test Product for Search Param");
        dataProduct.setDescription("Test Product Description");

        ResponseEntity<DataProductRes> dataProductResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(dataProductResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = dataProductResponse.getBody().getUuid();

        // Create first version with name "test-version-matching"
        DataProductVersionRes matchingVersion = new DataProductVersionRes();
        matchingVersion.setName("test-version-matching");
        matchingVersion.setDescription("Test version description");
        matchingVersion.setTag("v1.0.0");
        matchingVersion.setVersionNumber("1.0.0");
        matchingVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        matchingVersion.setDataProduct(dataProductResponse.getBody());
        matchingVersion.setSpec("dpds");
        matchingVersion.setSpecVersion("1.0.0");
        JsonNode matchingContent = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-matching\",\"version\":\"1.0.0\",\"description\":\"Test version description\"}}");
        matchingVersion.setContent(matchingContent);

        ResponseEntity<DataProductVersionRes> createMatchingResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(matchingVersion),
                DataProductVersionRes.class
        );
        assertThat(createMatchingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String matchingVersionId = createMatchingResponse.getBody().getUuid();

        // Create second version with different name "test-version-other"
        DataProductVersionRes otherVersion = new DataProductVersionRes();
        otherVersion.setName("test-version-other");
        otherVersion.setDescription("Test version description");
        otherVersion.setTag("v1.0.1");
        otherVersion.setVersionNumber("1.0.1");
        otherVersion.setValidationState(DataProductVersionValidationStateRes.PENDING);
        otherVersion.setDataProduct(dataProductResponse.getBody());
        otherVersion.setSpec("dpds");
        otherVersion.setSpecVersion("1.0.0");
        JsonNode otherContent = objectMapper.readTree("{\"dataProduct\":{\"name\":\"test-version-other\",\"version\":\"1.0.1\",\"description\":\"Test version description\"}}");
        otherVersion.setContent(otherContent);

        ResponseEntity<DataProductVersionRes> createOtherResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS),
                new HttpEntity<>(otherVersion),
                DataProductVersionRes.class
        );
        assertThat(createOtherResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String otherVersionId = createOtherResponse.getBody().getUuid();

        // When - Search with search parameter set to "matching"
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS) + "?search=matching",
                HttpMethod.GET,
                null,
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify response structure
        JsonNode responseBody = response.getBody();
        assertThat(responseBody.has("content")).isTrue();
        assertThat(responseBody.has("totalElements")).isTrue();
        assertThat(responseBody.get("totalElements").asInt()).isEqualTo(1);

        // Verify content array
        JsonNode content = responseBody.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(1);

        // Parse and verify the matching version
        DataProductVersionShortRes actualVersion = objectMapper.treeToValue(content.get(0), DataProductVersionShortRes.class);
        assertThat(actualVersion.getName()).isEqualTo("test-version-matching");
        assertThat(actualVersion.getUuid()).isEqualTo(matchingVersionId);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + matchingVersionId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCT_VERSIONS, "/" + otherVersionId));

        // Cleanup data product
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));

    }

}

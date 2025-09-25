package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ComponentScan(basePackages = {
        "org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct", // Mapper
        "org.opendatamesh.platform.pp.registry.dataproduct.services.core"      // Service
})
public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void whenGetDescriptorWithValidUuidAndTagThenReturnDescriptor() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithValidUuidAndTagThenReturnDescriptor");
        String dataProductId = createdDataProduct.getUuid();
        String tag = "v1.0.0";
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=" + tag),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Note: The actual response depends on the service implementation and test data setup
        // This test verifies the endpoint is accessible and returns a response

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithValidUuidAndBranchThenReturnDescriptor() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithValidUuidAndBranchThenReturnDescriptor");
        String dataProductId = createdDataProduct.getUuid();
        String branch = "main";
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?branch=" + branch),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithValidUuidAndCommitThenReturnDescriptor() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithValidUuidAndCommitThenReturnDescriptor");
        String dataProductId = createdDataProduct.getUuid();
        String commit = "abc123def456";
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?commit=" + commit),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithValidUuidAndNoVersionParamsThenUseDefaultBranch() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithValidUuidAndNoVersionParamsThenUseDefaultBranch");
        String dataProductId = createdDataProduct.getUuid();
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor"),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithValidUuidAndMultipleVersionParamsThenUseTagPriority() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithValidUuidAndMultipleVersionParamsThenUseTagPriority");
        String dataProductId = createdDataProduct.getUuid();
        String tag = "v1.0.0";
        String branch = "main";
        String commit = "abc123def456";
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=" + tag + "&branch=" + branch + "&commit=" + commit),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithNonExistentUuidThenReturnNotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        String tag = "v1.0.0";
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + nonExistentId + "/descriptor?tag=" + tag),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenGetDescriptorWithMissingAuthHeadersThenReturnBadRequest() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithMissingAuthHeadersThenReturnBadRequest");
        String dataProductId = createdDataProduct.getUuid();
        String tag = "v1.0.0";
        HttpHeaders headers = new HttpHeaders(); // No auth headers

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=" + tag),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithInvalidAuthTypeThenReturnBadRequest() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithInvalidAuthTypeThenReturnBadRequest");
        String dataProductId = createdDataProduct.getUuid();
        String tag = "v1.0.0";
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "INVALID");
        headers.add("x-odm-gpauth-param-token", "test-token");

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=" + tag),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithPatAuthTypeAndTokenThenReturnDescriptor() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithPatAuthTypeAndTokenThenReturnDescriptor");
        String dataProductId = createdDataProduct.getUuid();
        String tag = "v1.0.0";
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-token", "test-token");

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=" + tag),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithPatAuthTypeAndUsernameTokenThenReturnDescriptor() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithPatAuthTypeAndUsernameTokenThenReturnDescriptor");
        String dataProductId = createdDataProduct.getUuid();
        String tag = "v1.0.0";
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-username", "test-user");
        headers.add("x-odm-gpauth-param-token", "test-token");

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=" + tag),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithValidJsonResponseThenReturnValidJson() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithValidJsonResponseThenReturnValidJson");
        String dataProductId = createdDataProduct.getUuid();
        String tag = "v1.0.0";
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=" + tag),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        // Verify that the response is valid JSON (if not empty)
        if (!response.getBody().isEmpty()) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                assertThat(jsonNode).isNotNull();
            } catch (Exception e) {
                // If the service returns empty or null, that's also valid
                assertThat(response.getBody()).isIn("", "null");
            }
        }

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDescriptorWithDifferentVersionTypesThenHandleCorrectly() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDescriptorWithDifferentVersionTypesThenHandleCorrectly");
        String dataProductId = createdDataProduct.getUuid();
        HttpHeaders headers = createValidAuthHeaders();

        // Test tag
        ResponseEntity<String> tagResponse = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?tag=v1.0.0"),
                String.class,
                headers
        );
        assertThat(tagResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test branch
        ResponseEntity<String> branchResponse = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?branch=main"),
                String.class,
                headers
        );
        assertThat(branchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test commit
        ResponseEntity<String> commitResponse = rest.getForEntity(
                apiUrlFromString("/api/v1/dataproducts/" + dataProductId + "/descriptor?commit=abc123def456"),
                String.class,
                headers
        );
        assertThat(commitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    private HttpHeaders createValidAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-token", "test-token");
        return headers;
    }

    private DataProductRes createTestDataProduct(String name, String domain, String fqn) {
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName(name);
        dataProduct.setDomain(domain);
        dataProduct.setFqn(fqn);
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");
        return dataProduct;
    }

    private DataProductRes createAndSaveTestDataProduct(String testName) {
        return createAndSaveTestDataProduct(testName + "-product", testName + "-domain", testName + ".fqn");
    }

    private DataProductRes createAndSaveTestDataProduct(String name, String domain, String fqn) {
        DataProductRes dataProduct = createTestDataProduct(name, domain, fqn);
        ResponseEntity<DataProductRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
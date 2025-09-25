package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void whenGetDescriptorWithNonExistentUuidThenReturnNotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        String tag = "v1.0.0";
        HttpHeaders headers = createValidAuthHeaders();

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + nonExistentId + "/descriptor?tag=" + tag),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
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
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId + "/descriptor?tag=" + tag),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
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
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId + "/descriptor?tag=" + tag),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

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
package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class DataProductControllerIT extends RegistryApplicationIT {

    @Test
    public void whenCreateDataProductThenReturnCreatedDataProduct() {
        // Given
        DataProductRes dataProduct = createTestDataProduct("whenCreateDataProductThenReturnCreatedDataProduct");

        // When
        ResponseEntity<DataProductRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(dataProduct.getName());
        assertThat(response.getBody().getDomain()).isEqualTo(dataProduct.getDomain());
        assertThat(response.getBody().getFqn()).isEqualTo(dataProduct.getFqn());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + response.getBody().getUuid()));
    }

    @Test
    public void whenGetDataProductByIdThenReturnDataProduct() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenGetDataProductByIdThenReturnDataProduct");
        String dataProductId = createdDataProduct.getUuid();

        // When
        ResponseEntity<DataProductRes> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                DataProductRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo(dataProductId);
        assertThat(response.getBody().getName()).isEqualTo(createdDataProduct.getName());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenGetDataProductWithNonExistentIdThenReturnNotFound() {
        // Given
        String nonExistentId = "non-existent-id";

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + nonExistentId),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenSearchDataProductsThenReturnDataProductsList() {
        // Given
        DataProductRes dataProduct1 = createAndSaveTestDataProduct("whenSearchDataProductsThenReturnDataProductsList-1");
        DataProductRes dataProduct2 = createAndSaveTestDataProduct("whenSearchDataProductsThenReturnDataProductsList-2", "test-domain-2", "test.fqn.2");

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProduct1.getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProduct2.getUuid()));
    }

    @Test
    public void whenSearchDataProductsWithFiltersThenReturnFilteredResults() {
        // Given
        DataProductRes filteredProduct = createAndSaveTestDataProduct("whenSearchDataProductsWithFiltersThenReturnFilteredResults-filtered", "filtered-domain", "filtered.fqn");
        DataProductRes otherProduct = createAndSaveTestDataProduct("whenSearchDataProductsWithFiltersThenReturnFilteredResults-other", "other-domain", "other.fqn");

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "?domain=filtered-domain"),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + filteredProduct.getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + otherProduct.getUuid()));
    }

    @Test
    public void whenUpdateDataProductThenReturnUpdatedDataProduct() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenUpdateDataProductThenReturnUpdatedDataProduct");
        String dataProductId = createdDataProduct.getUuid();
        
        DataProductRes updatedDataProduct = createTestDataProduct("whenUpdateDataProductThenReturnUpdatedDataProduct-updated");
        updatedDataProduct.setUuid(dataProductId); // Set the UUID to match the existing data product
        updatedDataProduct.setDisplayName("Updated Display Name");
        updatedDataProduct.setDescription("Updated Description");

        // When
        ResponseEntity<DataProductRes> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                HttpMethod.PUT,
                new HttpEntity<>(updatedDataProduct),
                DataProductRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo(dataProductId);
        assertThat(response.getBody().getDisplayName()).isEqualTo("Updated Display Name");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Description");

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenDeleteDataProductThenReturnNoContentAndDataProductIsDeleted() {
        // Given
        DataProductRes createdDataProduct = createAndSaveTestDataProduct("whenDeleteDataProductThenReturnNoContentAndDataProductIsDeleted");
        String dataProductId = createdDataProduct.getUuid();

        // When
        ResponseEntity<Void> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify deletion
        ResponseEntity<String> getResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // No cleanup needed - resource is already deleted
    }

    @Test
    public void whenCreateDataProductWithInvalidDataThenReturnBadRequest() {
        // Given
        DataProductRes invalidDataProduct = new DataProductRes();
        // Missing required fields

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(invalidDataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenCreateDataProductWithDuplicateNameAndDomainThenReturnConflict() {
        // Given
        DataProductRes firstDataProduct = createTestDataProduct("whenCreateDataProductWithDuplicateNameAndDomainThenReturnConflict");
        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create another data product with same name and domain but different FQN
        DataProductRes duplicateDataProduct = createTestDataProduct(
                firstDataProduct.getName(), 
                firstDataProduct.getDomain(), 
                "different.fqn"
        );

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(duplicateDataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + firstResponse.getBody().getUuid()));
    }

    @Test
    public void whenCreateDataProductWithDuplicateFqnThenReturnConflict() {
        // Given
        DataProductRes firstDataProduct = createTestDataProduct("whenCreateDataProductWithDuplicateFqnThenReturnConflict");
        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create another data product with same FQN but different name and domain
        DataProductRes duplicateDataProduct = createTestDataProduct(
                "different-name", 
                "different-domain", 
                firstDataProduct.getFqn()
        );

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(duplicateDataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + firstResponse.getBody().getUuid()));
    }

    @Test
    public void whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict() {
        // Given
        DataProductRes firstDataProduct = createTestDataProduct("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-1");
        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        DataProductRes secondDataProduct = createTestDataProduct("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-2");
        ResponseEntity<DataProductRes> secondResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(secondDataProduct),
                DataProductRes.class
        );
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Try to update second data product to have same name and domain as first
        DataProductRes updatedDataProduct = createTestDataProduct(
                firstDataProduct.getName(), 
                firstDataProduct.getDomain(), 
                "different.fqn"
        );
        updatedDataProduct.setUuid(secondResponse.getBody().getUuid());

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + secondResponse.getBody().getUuid()),
                HttpMethod.PUT,
                new HttpEntity<>(updatedDataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + firstResponse.getBody().getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + secondResponse.getBody().getUuid()));
    }

    @Test
    public void whenUpdateDataProductWithDuplicateFqnThenReturnConflict() {
        // Given
        DataProductRes firstDataProduct = createTestDataProduct("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-1");
        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        DataProductRes secondDataProduct = createTestDataProduct("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-2");
        ResponseEntity<DataProductRes> secondResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(secondDataProduct),
                DataProductRes.class
        );
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Try to update second data product to have same FQN as first
        DataProductRes updatedDataProduct = createTestDataProduct(
                "different-name", 
                "different-domain", 
                firstDataProduct.getFqn()
        );
        updatedDataProduct.setUuid(secondResponse.getBody().getUuid());

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + secondResponse.getBody().getUuid()),
                HttpMethod.PUT,
                new HttpEntity<>(updatedDataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + firstResponse.getBody().getUuid()));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + secondResponse.getBody().getUuid()));
    }

    private DataProductRes createTestDataProduct(String testName) {
        return createTestDataProduct(testName + "-product", testName + "-domain", testName + ".fqn");
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

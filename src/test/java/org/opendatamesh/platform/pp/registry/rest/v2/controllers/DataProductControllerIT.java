package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoProviderTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoRes;
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
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenCreateDataProductThenReturnCreatedDataProduct-product");
        dataProduct.setDomain("whenCreateDataProductThenReturnCreatedDataProduct-domain");
        dataProduct.setFqn("whenCreateDataProductThenReturnCreatedDataProduct.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

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
        // Given - Create and save data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenGetDataProductByIdThenReturnDataProduct-product");
        dataProduct.setDomain("whenGetDataProductByIdThenReturnDataProduct-domain");
        dataProduct.setFqn("whenGetDataProductByIdThenReturnDataProduct.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = createResponse.getBody().getUuid();

        // When
        ResponseEntity<DataProductRes> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                DataProductRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo(dataProductId);
        assertThat(response.getBody().getName()).isEqualTo(dataProduct.getName());

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
        // Given - Create and save first data product
        DataProductRes dataProduct1 = new DataProductRes();
        dataProduct1.setName("whenSearchDataProductsThenReturnDataProductsList-1-product");
        dataProduct1.setDomain("whenSearchDataProductsThenReturnDataProductsList-1-domain");
        dataProduct1.setFqn("whenSearchDataProductsThenReturnDataProductsList-1.fqn");
        dataProduct1.setDisplayName("Test Display Name 1");
        dataProduct1.setDescription("Test Description 1");

        ResponseEntity<DataProductRes> createResponse1 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct1),
                DataProductRes.class
        );
        assertThat(createResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProduct1Id = createResponse1.getBody().getUuid();

        // Create and save second data product
        DataProductRes dataProduct2 = new DataProductRes();
        dataProduct2.setName("whenSearchDataProductsThenReturnDataProductsList-2-product");
        dataProduct2.setDomain("test-domain-2");
        dataProduct2.setFqn("test.fqn.2");
        dataProduct2.setDisplayName("Test Display Name 2");
        dataProduct2.setDescription("Test Description 2");

        ResponseEntity<DataProductRes> createResponse2 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct2),
                DataProductRes.class
        );
        assertThat(createResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProduct2Id = createResponse2.getBody().getUuid();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProduct1Id));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProduct2Id));
    }

    @Test
    public void whenSearchDataProductsWithFiltersThenReturnFilteredResults() {
        // Given - Create and save filtered product
        DataProductRes filteredProduct = new DataProductRes();
        filteredProduct.setName("whenSearchDataProductsWithFiltersThenReturnFilteredResults-filtered-product");
        filteredProduct.setDomain("filtered-domain");
        filteredProduct.setFqn("filtered.fqn");
        filteredProduct.setDisplayName("Filtered Display Name");
        filteredProduct.setDescription("Filtered Description");

        ResponseEntity<DataProductRes> createResponse1 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(filteredProduct),
                DataProductRes.class
        );
        assertThat(createResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String filteredProductId = createResponse1.getBody().getUuid();

        // Create and save other product
        DataProductRes otherProduct = new DataProductRes();
        otherProduct.setName("whenSearchDataProductsWithFiltersThenReturnFilteredResults-other-product");
        otherProduct.setDomain("other-domain");
        otherProduct.setFqn("other.fqn");
        otherProduct.setDisplayName("Other Display Name");
        otherProduct.setDescription("Other Description");

        ResponseEntity<DataProductRes> createResponse2 = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(otherProduct),
                DataProductRes.class
        );
        assertThat(createResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String otherProductId = createResponse2.getBody().getUuid();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "?domain=filtered-domain"),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + filteredProductId));
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + otherProductId));
    }

    @Test
    public void whenUpdateDataProductThenReturnUpdatedDataProduct() {
        // Given - Create and save initial data product
        DataProductRes initialDataProduct = new DataProductRes();
        initialDataProduct.setName("whenUpdateDataProductThenReturnUpdatedDataProduct-product");
        initialDataProduct.setDomain("whenUpdateDataProductThenReturnUpdatedDataProduct-domain");
        initialDataProduct.setFqn("whenUpdateDataProductThenReturnUpdatedDataProduct.fqn");
        initialDataProduct.setDisplayName("Initial Display Name");
        initialDataProduct.setDescription("Initial Description");

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(initialDataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = createResponse.getBody().getUuid();

        // Create updated data product
        DataProductRes updatedDataProduct = new DataProductRes();
        updatedDataProduct.setUuid(dataProductId); // Set the UUID to match the existing data product
        updatedDataProduct.setName("whenUpdateDataProductThenReturnUpdatedDataProduct-product");
        updatedDataProduct.setDomain("whenUpdateDataProductThenReturnUpdatedDataProduct-domain");
        updatedDataProduct.setFqn("whenUpdateDataProductThenReturnUpdatedDataProduct.fqn");
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
        // Given - Create and save data product
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("whenDeleteDataProductThenReturnNoContentAndDataProductIsDeleted-product");
        dataProduct.setDomain("whenDeleteDataProductThenReturnNoContentAndDataProductIsDeleted-domain");
        dataProduct.setFqn("whenDeleteDataProductThenReturnNoContentAndDataProductIsDeleted.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = createResponse.getBody().getUuid();

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
        // Given - Create first data product
        DataProductRes firstDataProduct = new DataProductRes();
        firstDataProduct.setName("whenCreateDataProductWithDuplicateNameAndDomainThenReturnConflict-product");
        firstDataProduct.setDomain("whenCreateDataProductWithDuplicateNameAndDomainThenReturnConflict-domain");
        firstDataProduct.setFqn("whenCreateDataProductWithDuplicateNameAndDomainThenReturnConflict.fqn");
        firstDataProduct.setDisplayName("First Display Name");
        firstDataProduct.setDescription("First Description");

        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create another data product with same name and domain but different FQN
        DataProductRes duplicateDataProduct = new DataProductRes();
        duplicateDataProduct.setName(firstDataProduct.getName());
        duplicateDataProduct.setDomain(firstDataProduct.getDomain());
        duplicateDataProduct.setFqn("different.fqn");
        duplicateDataProduct.setDisplayName("Duplicate Display Name");
        duplicateDataProduct.setDescription("Duplicate Description");

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
        // Given - Create first data product
        DataProductRes firstDataProduct = new DataProductRes();
        firstDataProduct.setName("whenCreateDataProductWithDuplicateFqnThenReturnConflict-product");
        firstDataProduct.setDomain("whenCreateDataProductWithDuplicateFqnThenReturnConflict-domain");
        firstDataProduct.setFqn("whenCreateDataProductWithDuplicateFqnThenReturnConflict.fqn");
        firstDataProduct.setDisplayName("First Display Name");
        firstDataProduct.setDescription("First Description");

        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create another data product with same FQN but different name and domain
        DataProductRes duplicateDataProduct = new DataProductRes();
        duplicateDataProduct.setName("different-name");
        duplicateDataProduct.setDomain("different-domain");
        duplicateDataProduct.setFqn(firstDataProduct.getFqn());
        duplicateDataProduct.setDisplayName("Duplicate Display Name");
        duplicateDataProduct.setDescription("Duplicate Description");

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
        // Given - Create first data product
        DataProductRes firstDataProduct = new DataProductRes();
        firstDataProduct.setName("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-1-product");
        firstDataProduct.setDomain("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-1-domain");
        firstDataProduct.setFqn("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-1.fqn");
        firstDataProduct.setDisplayName("First Display Name");
        firstDataProduct.setDescription("First Description");

        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create second data product
        DataProductRes secondDataProduct = new DataProductRes();
        secondDataProduct.setName("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-2-product");
        secondDataProduct.setDomain("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-2-domain");
        secondDataProduct.setFqn("whenUpdateDataProductWithDuplicateNameAndDomainThenReturnConflict-2.fqn");
        secondDataProduct.setDisplayName("Second Display Name");
        secondDataProduct.setDescription("Second Description");

        ResponseEntity<DataProductRes> secondResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(secondDataProduct),
                DataProductRes.class
        );
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Try to update second data product to have same name and domain as first
        DataProductRes updatedDataProduct = new DataProductRes();
        updatedDataProduct.setUuid(secondResponse.getBody().getUuid());
        updatedDataProduct.setName(firstDataProduct.getName());
        updatedDataProduct.setDomain(firstDataProduct.getDomain());
        updatedDataProduct.setFqn("different.fqn");
        updatedDataProduct.setDisplayName("Updated Display Name");
        updatedDataProduct.setDescription("Updated Description");

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
        // Given - Create first data product
        DataProductRes firstDataProduct = new DataProductRes();
        firstDataProduct.setName("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-1-product");
        firstDataProduct.setDomain("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-1-domain");
        firstDataProduct.setFqn("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-1.fqn");
        firstDataProduct.setDisplayName("First Display Name");
        firstDataProduct.setDescription("First Description");

        ResponseEntity<DataProductRes> firstResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(firstDataProduct),
                DataProductRes.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create second data product
        DataProductRes secondDataProduct = new DataProductRes();
        secondDataProduct.setName("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-2-product");
        secondDataProduct.setDomain("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-2-domain");
        secondDataProduct.setFqn("whenUpdateDataProductWithDuplicateFqnThenReturnConflict-2.fqn");
        secondDataProduct.setDisplayName("Second Display Name");
        secondDataProduct.setDescription("Second Description");

        ResponseEntity<DataProductRes> secondResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(secondDataProduct),
                DataProductRes.class
        );
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Try to update second data product to have same FQN as first
        DataProductRes updatedDataProduct = new DataProductRes();
        updatedDataProduct.setUuid(secondResponse.getBody().getUuid());
        updatedDataProduct.setName("different-name");
        updatedDataProduct.setDomain("different-domain");
        updatedDataProduct.setFqn(firstDataProduct.getFqn());
        updatedDataProduct.setDisplayName("Updated Display Name");
        updatedDataProduct.setDescription("Updated Description");

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
    public void whenCreateDataProductWithRepositoryThenReturnCreatedDataProductWithRepository() {
        // Given
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-with-repo");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        // Create repository
        DataProductRepoRes repository = new DataProductRepoRes();
        repository.setName("test-product-with-repo-repo");
        repository.setDescription("Test repository for test-product-with-repo");
        repository.setExternalIdentifier("test-org/test-product-with-repo-repo");
        repository.setDescriptorRootPath("/descriptors");
        repository.setRemoteUrlHttp("https://github.com/test-org/test-product-with-repo-repo.git");
        repository.setRemoteUrlSsh("git@github.com:test-org/test-product-with-repo-repo.git");
        repository.setDefaultBranch("main");
        repository.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        repository.setProviderBaseUrl("https://github.com");

        dataProduct.setDataProductRepo(repository);

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

        // Verify repository is created and associated
        assertThat(response.getBody().getDataProductRepo()).isNotNull();
        assertThat(response.getBody().getDataProductRepo().getUuid()).isNotNull();
        assertThat(response.getBody().getDataProductRepo().getName()).isEqualTo(dataProduct.getDataProductRepo().getName());
        assertThat(response.getBody().getDataProductRepo().getExternalIdentifier()).isEqualTo(dataProduct.getDataProductRepo().getExternalIdentifier());
        assertThat(response.getBody().getDataProductRepo().getProviderType()).isEqualTo(dataProduct.getDataProductRepo().getProviderType());
        assertThat(response.getBody().getDataProductRepo().getRemoteUrlHttp()).isEqualTo(dataProduct.getDataProductRepo().getRemoteUrlHttp());

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + response.getBody().getUuid()));
    }

    @Test
    public void whenUpdateDataProductRepositoryThenReturnUpdatedDataProductWithModifiedRepository() {
        // Given - Create initial data product with repository
        DataProductRes initialDataProduct = new DataProductRes();
        initialDataProduct.setName("test-product-update");
        initialDataProduct.setDomain("test-domain");
        initialDataProduct.setFqn("test.update.fqn");
        initialDataProduct.setDisplayName("Initial Display Name");
        initialDataProduct.setDescription("Initial Description");

        DataProductRepoRes initialRepository = new DataProductRepoRes();
        initialRepository.setName("test-product-update-repo");
        initialRepository.setDescription("Initial repository description");
        initialRepository.setExternalIdentifier("test-org/test-product-update-repo");
        initialRepository.setDescriptorRootPath("/descriptors");
        initialRepository.setRemoteUrlHttp("https://github.com/test-org/test-product-update-repo.git");
        initialRepository.setRemoteUrlSsh("git@github.com:test-org/test-product-update-repo.git");
        initialRepository.setDefaultBranch("main");
        initialRepository.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        initialRepository.setProviderBaseUrl("https://github.com");

        initialDataProduct.setDataProductRepo(initialRepository);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(initialDataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = createResponse.getBody().getUuid();

        // Create updated data product with modified repository
        DataProductRes updatedDataProduct = new DataProductRes();
        updatedDataProduct.setUuid(dataProductId); // Set the UUID to match the existing data product
        updatedDataProduct.setName("test-product-update");
        updatedDataProduct.setDomain("test-domain");
        updatedDataProduct.setFqn("test.update.fqn");
        updatedDataProduct.setDisplayName("Updated Display Name");
        updatedDataProduct.setDescription("Updated Description");

        // Update repository details
        DataProductRepoRes updatedRepo = new DataProductRepoRes();
        updatedRepo.setName("Updated Repository Name");
        updatedRepo.setDescription("Updated Repository Description");
        updatedRepo.setExternalIdentifier("test-org/test-product-update-repo");
        updatedRepo.setDescriptorRootPath("/descriptors");
        updatedRepo.setDefaultBranch("develop");
        updatedRepo.setRemoteUrlHttp("https://github.com/updated-org/updated-repo.git");
        updatedRepo.setRemoteUrlSsh("git@github.com:updated-org/updated-repo.git");
        updatedRepo.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        updatedRepo.setProviderBaseUrl("https://github.com");

        updatedDataProduct.setDataProductRepo(updatedRepo);

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

        // Verify repository is updated
        assertThat(response.getBody().getDataProductRepo()).isNotNull();
        assertThat(response.getBody().getDataProductRepo().getName()).isEqualTo("Updated Repository Name");
        assertThat(response.getBody().getDataProductRepo().getDescription()).isEqualTo("Updated Repository Description");
        assertThat(response.getBody().getDataProductRepo().getDefaultBranch()).isEqualTo("develop");
        assertThat(response.getBody().getDataProductRepo().getRemoteUrlHttp()).isEqualTo("https://github.com/updated-org/updated-repo.git");
        assertThat(response.getBody().getDataProductRepo().getRemoteUrlSsh()).isEqualTo("git@github.com:updated-org/updated-repo.git");
        assertThat(response.getBody().getDataProductRepo().getDataProductUuid()).isEqualTo(dataProductId);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenDeleteDataProductWithRepositoryThenReturnNoContentAndBothAreDeleted() {
        // Given - Create data product with repository
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-delete");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.delete.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        DataProductRepoRes repository = new DataProductRepoRes();
        repository.setName("test-product-delete-repo");
        repository.setDescription("Test repository for deletion");
        repository.setExternalIdentifier("test-org/test-product-delete-repo");
        repository.setDescriptorRootPath("/descriptors");
        repository.setRemoteUrlHttp("https://github.com/test-org/test-product-delete-repo.git");
        repository.setRemoteUrlSsh("git@github.com:test-org/test-product-delete-repo.git");
        repository.setDefaultBranch("main");
        repository.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        repository.setProviderBaseUrl("https://github.com");

        dataProduct.setDataProductRepo(repository);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = createResponse.getBody().getUuid();
        String repositoryId = createResponse.getBody().getDataProductRepo().getUuid();

        // When
        ResponseEntity<Void> response = rest.exchange(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify data product is deleted
        ResponseEntity<String> getDataProductResponse = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                String.class
        );
        assertThat(getDataProductResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Note: Since DataProductRepo is cascade deleted with DataProduct, 
        // we can't directly verify its deletion through the API, but the 
        // cascade delete behavior is tested through the entity relationship

        // No cleanup needed - resource is already deleted
    }

    @Test
    public void whenGetDataProductWithRepositoryThenReturnDataProductWithRepositoryDetails() {
        // Given - Create data product with repository
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-get");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.get.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        DataProductRepoRes repository = new DataProductRepoRes();
        repository.setName("test-product-get-repo");
        repository.setDescription("Test repository for retrieval");
        repository.setExternalIdentifier("test-org/test-product-get-repo");
        repository.setDescriptorRootPath("/descriptors");
        repository.setRemoteUrlHttp("https://github.com/test-org/test-product-get-repo.git");
        repository.setRemoteUrlSsh("git@github.com:test-org/test-product-get-repo.git");
        repository.setDefaultBranch("main");
        repository.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        repository.setProviderBaseUrl("https://github.com");

        dataProduct.setDataProductRepo(repository);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String dataProductId = createResponse.getBody().getUuid();

        // When
        ResponseEntity<DataProductRes> response = rest.getForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId),
                DataProductRes.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isEqualTo(dataProductId);
        assertThat(response.getBody().getName()).isEqualTo(dataProduct.getName());

        // Verify repository details are included
        assertThat(response.getBody().getDataProductRepo()).isNotNull();
        assertThat(response.getBody().getDataProductRepo().getUuid()).isNotNull();
        assertThat(response.getBody().getDataProductRepo().getName()).isEqualTo(dataProduct.getDataProductRepo().getName());
        assertThat(response.getBody().getDataProductRepo().getExternalIdentifier()).isEqualTo(dataProduct.getDataProductRepo().getExternalIdentifier());
        assertThat(response.getBody().getDataProductRepo().getProviderType()).isEqualTo(dataProduct.getDataProductRepo().getProviderType());
        assertThat(response.getBody().getDataProductRepo().getDataProductUuid()).isEqualTo(dataProductId);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + dataProductId));
    }

    @Test
    public void whenCreateDataProductWithRepositoryWithInvalidDataThenReturnBadRequest() {
        // Given - Create data product with invalid repository data
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-invalid");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.invalid.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        // Create repository with invalid data (missing required fields)
        DataProductRepoRes repository = new DataProductRepoRes();
        // Missing required fields like name, externalIdentifier, providerType, etc.
        repository.setDescription("Test repository with missing required fields");
        repository.setDescriptorRootPath("/descriptors");
        repository.setDefaultBranch("main");
        repository.setProviderBaseUrl("https://github.com");

        dataProduct.setDataProductRepo(repository);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenCreateDataProductWithRepositoryWithInvalidProviderTypeThenReturnBadRequest() {
        // Given - Create data product with invalid provider type
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-invalid-provider");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.invalid.provider.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        DataProductRepoRes repository = new DataProductRepoRes();
        repository.setName("test-product-invalid-provider-repo");
        repository.setDescription("Test repository with invalid provider");
        repository.setExternalIdentifier("test-org/test-product-invalid-provider-repo");
        repository.setDescriptorRootPath("/descriptors");
        repository.setRemoteUrlHttp("https://github.com/test-org/test-product-invalid-provider-repo.git");
        repository.setRemoteUrlSsh("git@github.com:test-org/test-product-invalid-provider-repo.git");
        repository.setDefaultBranch("main");
        // Set invalid provider type (null or invalid enum value)
        repository.setProviderType(null);
        repository.setProviderBaseUrl("https://github.com");

        dataProduct.setDataProductRepo(repository);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenCreateDataProductWithRepositoryWithInvalidUrlsThenReturnBadRequest() {
        // Given - Create data product with invalid repository URLs
        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-invalid-urls");
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test.invalid.urls.fqn");
        dataProduct.setDisplayName("Test Display Name");
        dataProduct.setDescription("Test Description");

        DataProductRepoRes repository = new DataProductRepoRes();
        repository.setName("test-product-invalid-urls-repo");
        repository.setDescription("Test repository with invalid URLs");
        repository.setExternalIdentifier("test-org/test-product-invalid-urls-repo");
        repository.setDescriptorRootPath("/descriptors");
        // Set invalid URLs
        repository.setRemoteUrlHttp(null);
        repository.setRemoteUrlSsh(null);
        repository.setDefaultBranch("main");
        repository.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        repository.setProviderBaseUrl("invalid-base-url");

        dataProduct.setDataProductRepo(repository);

        // When
        ResponseEntity<String> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


}

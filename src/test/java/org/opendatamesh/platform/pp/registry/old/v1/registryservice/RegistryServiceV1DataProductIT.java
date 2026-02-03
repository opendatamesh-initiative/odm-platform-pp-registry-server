package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.opendatamesh.dpds.parser.IdentifierStrategyFactory;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryServiceV1DataProductIT extends RegistryApplicationIT {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    /*
    Feature: Backward compatibility for legacy Registry data product endpoint (GET product by id)

    Resource: RegistryV1DataProductResource { id, fullyQualifiedName, description, domain }

    Scenario: Retrieve data product by uuid generated from the hash of its FQN
        Given a Data Product with a known FQN exists (created via V2 API)
        When GET /api/v1/pp/registry/products/{uuid} with uuid = IdentifierStrategy.getId(fqn)
        Then the response status is 200
        And the response body is a RegistryV1DataProductResource with matching fullyQualifiedName, description, domain, and id
    */
    @Test
    void whenGetDataProductByFqnDerivedIdThenReturnRegistryV1DataProductResource() {
        // Given: a Data Product with a known FQN exists (created via V2 API)
        String fqn = "test.product.v1.get.by.fqn.id";
        String domain = "test-domain";
        String description = "Test Product Description";

        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-v1-fqn-id");
        dataProduct.setDomain(domain);
        dataProduct.setFqn(fqn);
        dataProduct.setDisplayName("Test Product V1 FQN Id");
        dataProduct.setDescription(description);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productUuid = createResponse.getBody().getUuid();

        String fqnDerivedId = IdentifierStrategyFactory.getDefault("org.opendatamesh").getId(fqn);

        // When: GET /api/v1/pp/registry/products/{uuid} with uuid = IdentifierStrategy.getId(fqn)
        ResponseEntity<RegistryV1DataProductResource> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/products/" + fqnDerivedId),
                HttpMethod.GET,
                null,
                RegistryV1DataProductResource.class
        );

        // Then: response status 200 and body is RegistryV1DataProductResource with matching fields
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(productUuid);
        assertThat(response.getBody().getFullyQualifiedName()).isEqualTo(fqn);
        assertThat(response.getBody().getDescription()).isEqualTo(description);
        assertThat(response.getBody().getDomain()).isEqualTo(domain);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + productUuid));
    }

    /*
   Scenario: Retrieve data product by real V2 data product uuid
        Given a Data Product exists (created via V2 API)
        When GET /api/v1/pp/registry/products/{uuid} with uuid = dataProduct.getUuid()
        Then the response status is 200
        And the response body is a RegistryV1DataProductResource with same product data (fqn, description, domain, id = uuid)
    */
    @Test
    void whenGetDataProductByRealUuidThenReturnRegistryV1DataProductResource() {
        // Given: a Data Product exists (created via V2 API)
        String fqn = "test.product.v1.get.by.real.uuid";
        String domain = "test-domain";
        String description = "Test Product Description Real Uuid";

        DataProductRes dataProduct = new DataProductRes();
        dataProduct.setName("test-product-v1-real-uuid");
        dataProduct.setDomain(domain);
        dataProduct.setFqn(fqn);
        dataProduct.setDisplayName("Test Product V1 Real Uuid");
        dataProduct.setDescription(description);

        ResponseEntity<DataProductRes> createResponse = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProduct),
                DataProductRes.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productUuid = createResponse.getBody().getUuid();

        // When: GET /api/v1/pp/registry/products/{uuid} with uuid = dataProduct.getUuid()
        ResponseEntity<RegistryV1DataProductResource> response = rest.exchange(
                apiUrlFromString("/api/v1/pp/registry/products/" + productUuid),
                HttpMethod.GET,
                null,
                RegistryV1DataProductResource.class
        );

        // Then: response status 200 and body is RegistryV1DataProductResource with same product data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(productUuid);
        assertThat(response.getBody().getFullyQualifiedName()).isEqualTo(fqn);
        assertThat(response.getBody().getDescription()).isEqualTo(description);
        assertThat(response.getBody().getDomain()).isEqualTo(domain);

        // Cleanup
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + productUuid));
    }
}

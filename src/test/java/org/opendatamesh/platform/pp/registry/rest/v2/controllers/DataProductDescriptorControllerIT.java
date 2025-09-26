package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductsDescriptorService;
import org.opendatamesh.platform.pp.registry.dataproduct.services.GitReference;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Optional;


@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.hibernate.ddl-auto=none"
        }
)
class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private DataProductsDescriptorService dataProductsDescriptorService;

    @Test
    void whenGetDataProductDescriptorByIdThenReturnDataProduct() throws Exception {
        // Given
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        String descriptorJson = """
            { "name": "my-data-product", "version": "1.0" }
            """;

        JsonNode jsonNode = new ObjectMapper().readTree(descriptorJson);

        Mockito.when(dataProductsDescriptorService.getDescriptor(
                Mockito.eq(uuid),
                Mockito.any(GitReference.class),
                Mockito.any(Credential.class)
        )).thenReturn(Optional.of(jsonNode));

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-username", "user");
        headers.add("x-odm-gpauth-param-token", "token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v2/pp/registry/products/" + uuid + "/descriptor",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("my-data-product"));
        Assertions.assertTrue(response.getBody().contains("1.0"));
    }

    @Test
    void whenDescriptorNotFound_thenReturnEmptyBody() throws Exception {
        // Given
        String uuid = "non-existing-uuid";

        Mockito.when(dataProductsDescriptorService.getDescriptor(
                Mockito.eq(uuid),
                Mockito.any(GitReference.class),
                Mockito.any(Credential.class)
        )).thenReturn(Optional.empty());

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-username", "user");
        headers.add("x-odm-gpauth-param-token", "token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v2/pp/registry/products/" + uuid + "/descriptor",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().isEmpty() || response.getBody().equals("null"));
    }

    @Test
    void whenMissingCredentials_thenReturnBadRequest() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";

        HttpHeaders headers = new HttpHeaders(); // No credentials
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v2/pp/registry/products/" + uuid + "/descriptor",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("Missing or invalid credentials"));
    }

    @Test
    void whenDescriptorRequestedWithBranch_thenServiceCalledWithCorrectReference() throws Exception {
        // Given
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        String branch = "feature-branch";
        String descriptorJson = "{ \"name\": \"branch-data-product\", \"version\": \"2.0\" }";

        JsonNode jsonNode = new ObjectMapper().readTree(descriptorJson);

        Mockito.when(dataProductsDescriptorService.getDescriptor(
                Mockito.eq(uuid),
                Mockito.argThat(ref -> branch.equals(ref.getBranch())),
                Mockito.any(Credential.class)
        )).thenReturn(Optional.of(jsonNode));

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-username", "user");
        headers.add("x-odm-gpauth-param-token", "token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v2/pp/registry/products/" + uuid + "/descriptor?branch=" + branch,
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("branch-data-product"));
        Assertions.assertTrue(response.getBody().contains("2.0"));
    }

}


package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.dataproduct.repositories.DataProductsRepository;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    @Autowired
    private DataProductsRepository dataProductsRepository;

    private GitProvider mockGitProvider;
    private DataProduct testDataProduct;
    private DataProductRepo testDataProductRepo;

    @BeforeEach
    void setUp() {
        mockGitProvider = Mockito.mock(GitProvider.class);
        gitProviderFactoryMock.setMockGitProvider(mockGitProvider);
    }

    private DataProduct createAndSaveTestDataProduct(String name, String externalIdentifier, DataProductRepoProviderType providerType) {
        // Setup test data product (let database generate UUID)
        DataProduct dataProduct = new DataProduct();
        dataProduct.setName(name);
        dataProduct.setDomain("test-domain");
        dataProduct.setFqn("test-domain/" + name.toLowerCase().replace(" ", "-"));

        // Setup test data product repo (let database generate UUID)
        DataProductRepo dataProductRepo = new DataProductRepo();
        dataProductRepo.setExternalIdentifier(externalIdentifier);
        dataProductRepo.setName(name + " Repository");
        dataProductRepo.setDescriptorRootPath("data-product-descriptor.json");
        dataProductRepo.setProviderType(providerType);
        dataProductRepo.setProviderBaseUrl(providerType == DataProductRepoProviderType.GITHUB ? "https://github.com" : "https://gitlab.com");
        dataProductRepo.setDataProduct(dataProduct);
        dataProduct.setDataProductRepo(dataProductRepo);

        // Save to database and return with generated UUID
        return dataProductsRepository.save(dataProduct);
    }

    @Test
    void testGetDescriptorWithTag() throws IOException {
        // Given
        String testTag = "v1.0.0";
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product"
                    }
                }
                """;

        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
            Path tempRepoDir = Files.createTempDirectory("test-repo");
            Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
            Files.write(descriptorFile, testDescriptorContent.getBytes());
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/test-repo");
            mockRepository.setName("test-repo");

            // Mock GitProvider behavior - this simulates the getGitProvider() method in the service
            when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));
            when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor?tag=" + testTag;
            ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            // Then - simplified assertions
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("info").get("name").asText()).isEqualTo("Test Data Product");

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("test-org/test-repo");
            verify(mockGitProvider).readRepository(any(RepositoryPointerTag.class));

            // Cleanup temp files
            deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorWithBranch() throws IOException {
        // Given
        String testBranch = "main";
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product from main branch"
                    }
                }
                """;

        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));
        when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor?branch=" + testBranch;
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Then - simplified assertions
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("info").get("name").asText()).isEqualTo("Test Data Product");

        // Verify interactions with mocked dependencies
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup temp files
        deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorWithCommit() throws IOException {
        // Given
        String testCommit = "abc123def456";
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product from specific commit"
                    }
                }
                """;

        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));
        when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor?commit=" + testCommit;
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Then - simplified assertions
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("info").get("name").asText()).isEqualTo("Test Data Product");

        // Verify interactions with mocked dependencies
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerCommit.class));

        // Cleanup temp files
        deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorWithDefaultBranch() throws IOException {
        // Given
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product from default branch"
                    }
                }
                """;

        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));
        when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When - no tag, branch, or commit specified (should default to main branch)
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Then - simplified assertions
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("info").get("name").asText()).isEqualTo("Test Data Product");

        // Verify interactions with mocked dependencies
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup temp files
        deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorNotFound() {
        // Given
        String testUuid = "non-existent-uuid";
        
        // Don't save any data product to database - it should not be found

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

        // Then - should return 404 error when data product is not found
        assertThat(response.getStatusCode().value()).isEqualTo(404);

        // Verify interactions with mocked dependencies
        // Should not call git provider when data product is not found
        verify(mockGitProvider, never()).getRepository(any());
        verify(mockGitProvider, never()).readRepository(any());
    }

    @Test
    void testGetDescriptorMissingCredentials() {
        // Given
        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers without credentials
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

            // Then
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).contains("Missing or invalid credentials");

            // Verify no interactions with mocked dependencies
            verify(mockGitProvider, never()).getRepository(any());
            verify(mockGitProvider, never()).readRepository(any());
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorInvalidCredentials() {
        // Given
        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers with invalid credentials
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "INVALID");
            headers.set("x-odm-gpauth-param-token", "test-token");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

            // Then
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).contains("Missing or invalid credentials");

            // Verify no interactions with mocked dependencies
            verify(mockGitProvider, never()).getRepository(any());
            verify(mockGitProvider, never()).readRepository(any());
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorWithUsernameAndToken() throws IOException {
        // Given
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product with username"
                    }
                }
                """;

        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));
        when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

        // Setup headers with username and token
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-username", "testuser");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Then - simplified assertions
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("info").get("name").asText()).isEqualTo("Test Data Product");

        // Verify interactions with mocked dependencies
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup temp files
        deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorWithMultipleParameters() throws IOException {
        // Given
        String testTag = "v1.0.0";
        String testBranch = "main";
        String testCommit = "abc123def456";
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product with multiple parameters"
                    }
                }
                """;

        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
            Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));
        when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When - tag should take precedence over branch and commit
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor?tag=" + testTag + 
                    "&branch=" + testBranch + "&commit=" + testCommit;
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Then - simplified assertions
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("info").get("name").asText()).isEqualTo("Test Data Product");

        // Verify interactions with mocked dependencies
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerTag.class));

        // Cleanup temp files
        deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorWithGitLabProvider() throws IOException {
        // Given
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "GitLab Data Product",
                        "version": "1.0.0",
                        "description": "A test data product from GitLab"
                    }
                }
                """;

        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("GitLab Data Product", "gitlab-org/gitlab-repo", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
            Path tempRepoDir = Files.createTempDirectory("test-repo-gitlab");
            Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
            Files.write(descriptorFile, testDescriptorContent.getBytes());
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-org/gitlab-repo");
            mockRepository.setName("gitlab-repo");

        // Mock GitProvider behavior - this simulates the getGitProvider() method in the service
        when(mockGitProvider.getRepository("gitlab-org/gitlab-repo")).thenReturn(Optional.of(mockRepository));
        when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "gitlab-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Then - simplified assertions
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("info").get("name").asText()).isEqualTo("GitLab Data Product");

        // Verify interactions with mocked dependencies
        verify(mockGitProvider).getRepository("gitlab-org/gitlab-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup temp files
        deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorFileNotFound() throws IOException {
        // Given
        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();
        
        try {
            // Create temporary directory structure without the descriptor file
            Path tempRepoDir = Files.createTempDirectory("test-repo");
        // Don't create the descriptor file - it should be missing
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));
        when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<JsonNode> response = rest.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        // Then - should return null when descriptor file is not found
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(NullNode.class);

        // Verify interactions with mocked dependencies
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup temp files
        deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    @Test
    void testGetDescriptorRepositoryNotFound() {
        // Given
        // Create and save test data product
        DataProduct testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Mock GitProvider to return empty (repository not found)
            when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.empty());

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

            // Then - should return 400 error when repository is not found
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).contains("No remote repository was found");

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("test-org/test-repo");
            // Should not call readRepository when repository is not found
            verify(mockGitProvider, never()).readRepository(any());
        } finally {
            // Cleanup database
            dataProductsRepository.deleteById(testUuid);
        }
    }

    private void deleteRecursively(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                // Ignore cleanup errors
                            }
                        });
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }
}

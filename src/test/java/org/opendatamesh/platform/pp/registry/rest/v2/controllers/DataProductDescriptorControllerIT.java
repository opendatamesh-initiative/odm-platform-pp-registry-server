package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SuppressWarnings({"ConstantConditions", "DataFlowIssue", "NullAway", "PotentialNullPointerException", "NullPointerException", "all"})
public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    @MockitoBean
    private DataProductsService dataProductsService;

    private GitProvider mockGitProvider;
    private DataProduct testDataProduct;
    private DataProductRepo testDataProductRepo;

    @BeforeEach
    void setUp() {
        mockGitProvider = Mockito.mock(GitProvider.class);
        gitProviderFactoryMock.setMockGitProvider(mockGitProvider);

        // Setup test data product
        testDataProduct = new DataProduct();
        testDataProduct.setUuid("test-uuid-123");
        testDataProduct.setName("Test Data Product");
        testDataProduct.setDomain("test-domain");
        testDataProduct.setFqn("test-domain/test-data-product");

        // Setup test data product repo
        testDataProductRepo = new DataProductRepo();
        testDataProductRepo.setUuid("repo-uuid-123");
        testDataProductRepo.setExternalIdentifier("test-org/test-repo");
        testDataProductRepo.setName("Test Repository");
        testDataProductRepo.setDescriptorRootPath("data-product-descriptor.json");
        testDataProductRepo.setProviderType(DataProductRepoProviderType.GITHUB);
        testDataProductRepo.setProviderBaseUrl("https://github.com");
        testDataProductRepo.setDataProduct(testDataProduct);
        testDataProduct.setDataProductRepo(testDataProductRepo);
    }

    @Test
    void testGetDescriptorWithTag() throws IOException {
        // Given
        String testUuid = "test-uuid-123";
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

        // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerTag.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorWithBranch() throws IOException {
        // Given
        String testUuid = "test-uuid-123";
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

        // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorWithCommit() throws IOException {
        // Given
        String testUuid = "test-uuid-123";
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

        // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerCommit.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorWithDefaultBranch() throws IOException {
        // Given
        String testUuid = "test-uuid-123";
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

        // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorNotFound() {
        // Given
        String testUuid = "non-existent-uuid";
        
        // Mock DataProductsService to return null (data product not found)
        when(dataProductsService.findOne(testUuid)).thenReturn(null);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

        // Then - should return 500 error when data product is not found
        assertThat(response.getStatusCode().value()).isEqualTo(500);

        // Verify interactions with mocked dependencies
        verify(dataProductsService).findOne(testUuid);
        // Should not call git provider when data product is not found
        verify(mockGitProvider, never()).getRepository(any());
        verify(mockGitProvider, never()).readRepository(any());
    }

    @Test
    void testGetDescriptorMissingCredentials() {
        // Given
        String testUuid = "test-uuid-123";
        
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
        verify(dataProductsService, never()).findOne(any());
        verify(mockGitProvider, never()).getRepository(any());
        verify(mockGitProvider, never()).readRepository(any());
    }

    @Test
    void testGetDescriptorInvalidCredentials() {
        // Given
        String testUuid = "test-uuid-123";
        
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
        verify(dataProductsService, never()).findOne(any());
        verify(mockGitProvider, never()).getRepository(any());
        verify(mockGitProvider, never()).readRepository(any());
    }

    @Test
    void testGetDescriptorWithUsernameAndToken() throws IOException {
        // Given
        String testUuid = "test-uuid-123";
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

        // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorWithMultipleParameters() throws IOException {
        // Given
        String testUuid = "test-uuid-123";
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

        // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerTag.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorWithGitLabProvider() throws IOException {
        // Given
        String testUuid = "test-uuid-gitlab";
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

        // Create temporary directory structure that mimics a real repository
        Path tempRepoDir = Files.createTempDirectory("test-repo-gitlab");
        Path descriptorFile = tempRepoDir.resolve("data-product-descriptor.json");
        Files.write(descriptorFile, testDescriptorContent.getBytes());
        File mockRepoFile = tempRepoDir.toFile();

        // Setup GitLab data product
        DataProduct gitlabDataProduct = new DataProduct();
        gitlabDataProduct.setUuid(testUuid);
        gitlabDataProduct.setName("GitLab Data Product");
        gitlabDataProduct.setDomain("gitlab-domain");
        gitlabDataProduct.setFqn("gitlab-domain/gitlab-data-product");

        DataProductRepo gitlabDataProductRepo = new DataProductRepo();
        gitlabDataProductRepo.setUuid("repo-uuid-gitlab");
        gitlabDataProductRepo.setExternalIdentifier("gitlab-org/gitlab-repo");
        gitlabDataProductRepo.setName("GitLab Repository");
        gitlabDataProductRepo.setDescriptorRootPath("data-product-descriptor.json");
        gitlabDataProductRepo.setProviderType(DataProductRepoProviderType.GITLAB);
        gitlabDataProductRepo.setProviderBaseUrl("https://gitlab.com");
        gitlabDataProductRepo.setDataProduct(gitlabDataProduct);
        gitlabDataProduct.setDataProductRepo(gitlabDataProductRepo);

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("gitlab-org/gitlab-repo");
        mockRepository.setName("gitlab-repo");

        // Mock DataProductsService to return our GitLab data product
        when(dataProductsService.findOne(testUuid)).thenReturn(gitlabDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("gitlab-org/gitlab-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorFileNotFound() throws IOException {
        // Given
        String testUuid = "test-uuid-123";
        
        // Create temporary directory structure without the descriptor file
        Path tempRepoDir = Files.createTempDirectory("test-repo");
        // Don't create the descriptor file - it should be missing
        File mockRepoFile = tempRepoDir.toFile();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-org/test-repo");
        mockRepository.setName("test-repo");

        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));

        // Cleanup
        deleteRecursively(tempRepoDir);
    }

    @Test
    void testGetDescriptorRepositoryNotFound() {
        // Given
        String testUuid = "test-uuid-123";
        
        // Mock DataProductsService to return our test data product
        when(dataProductsService.findOne(testUuid)).thenReturn(testDataProduct);

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
        verify(dataProductsService).findOne(testUuid);
        verify(mockGitProvider).getRepository("test-org/test-repo");
        // Should not call readRepository when repository is not found
        verify(mockGitProvider, never()).readRepository(any());
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

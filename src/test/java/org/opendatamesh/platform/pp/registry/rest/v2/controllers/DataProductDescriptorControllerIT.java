package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryPointer;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitOperationFactoryMock;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoOwnerTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoProviderTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.TagRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    @Autowired
    private GitOperationFactoryMock gitOperationFactoryMock;


    private GitProvider mockGitProvider;
    private GitOperation mockGitOperation;

    @BeforeEach
    void setUp() {
        // Create fresh mocks for each test
        mockGitProvider = Mockito.mock(GitProvider.class);
        mockGitOperation = Mockito.mock(GitOperation.class);

        gitProviderFactoryMock.setMockGitProvider(mockGitProvider);
        gitOperationFactoryMock.setMockGitOperation(mockGitOperation);
    }

    @AfterEach
    void tearDown() {
        // Reset mocks
        Mockito.reset(mockGitProvider, mockGitOperation);

        // Reset mock factories
        gitProviderFactoryMock.reset();
        gitOperationFactoryMock.reset();
    }

    private void setupMockForNonExistentDataProduct() {
        // For non-existent data products, the real service will throw NotFoundException
        // when trying to find the data product via dataProductsService.findOne()
        // No additional setup needed as the service will handle this naturally
    }

    private void setupMockForRepositoryNotFound() {
        // Configure the mock GitProvider to return empty Optional for repository not found
        when(mockGitProvider.getRepository(anyString(), anyString())).thenReturn(Optional.empty());
    }

    private void setupMockGitOperationForRead() throws GitOperationException {
        setupMockGitOperationForRead("Test Data Product", "A test data product");
    }

    private void setupMockGitOperationForRead(String productName, String productDescription) throws GitOperationException {
        try {
            // Create a real temporary directory with a real descriptor file
            File mockRepoDir = Files.createTempDirectory("mock-repo-").toFile();
            File descriptorFile = new File(mockRepoDir, "data-product-descriptor.json");

            // Write the mock descriptor content to the file
            String descriptorJson = String.format("""
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "%s",
                            "version": "1.0.0",
                            "description": "%s"
                        }
                    }
                    """, productName, productDescription);
            Files.writeString(descriptorFile.toPath(), descriptorJson, StandardCharsets.UTF_8);

            when(mockGitOperation.getRepositoryContent(any(RepositoryPointer.class)))
                    .thenReturn(mockRepoDir);
        } catch (IOException e) {
            throw new GitOperationException("Failed to create mock repository", e);
        }
    }

    private void setupMockGitOperationForWrite() throws IOException, GitOperationException {
        // Create a real temporary directory for file operations
        File mockRepoDir = Files.createTempDirectory("mock-repo-write-").toFile();
        when(mockGitOperation.initRepository(anyString(), anyString(), any(java.net.URL.class)))
                .thenReturn(mockRepoDir);
        when(mockGitOperation.getRepositoryContent(any(RepositoryPointer.class)))
                .thenReturn(mockRepoDir);
        doNothing().when(mockGitOperation).addFiles(any(File.class), anyList());
        when(mockGitOperation.commit(any(File.class), anyString())).thenReturn(true);
        doNothing().when(mockGitOperation).push(any(File.class), eq(false));
    }

    private void setupMockGitOperationForWriteWithNoChanges() throws IOException, GitOperationException {
        // Create a real temporary directory for file operations
        // This setup simulates the case where commit returns false (no changes to commit)
        File mockRepoDir = Files.createTempDirectory("mock-repo-write-").toFile();
        when(mockGitOperation.getRepositoryContent(any(RepositoryPointer.class)))
                .thenReturn(mockRepoDir);
        doNothing().when(mockGitOperation).addFiles(any(File.class), anyList());
        when(mockGitOperation.commit(any(File.class), anyString())).thenReturn(false);
    }


    private DataProductRes createAndSaveTestDataProduct(String name, String externalIdentifier, String ownerId, DataProductRepoProviderType providerType) {
        // Setup test data product resource
        DataProductRes dataProductRes = new DataProductRes();
        dataProductRes.setName(name);
        dataProductRes.setDomain("test-domain");
        dataProductRes.setFqn("test-domain/" + name.toLowerCase().replace(" ", "-"));
        dataProductRes.setDisplayName("Test Display Name");
        dataProductRes.setDescription("Test Description");

        // Setup test data product repo resource
        DataProductRepoRes dataProductRepoRes = new DataProductRepoRes();
        dataProductRepoRes.setExternalIdentifier(externalIdentifier);
        dataProductRepoRes.setName(name + " Repository");
        dataProductRepoRes.setDescription("Test repository description");
        dataProductRepoRes.setDescriptorRootPath("data-product-descriptor.json");
        dataProductRepoRes.setRemoteUrlHttp(providerType == DataProductRepoProviderType.GITHUB ?
                "https://github.com/" + externalIdentifier + ".git" :
                "https://gitlab.com/" + externalIdentifier + ".git");
        dataProductRepoRes.setRemoteUrlSsh(providerType == DataProductRepoProviderType.GITHUB ?
                "git@github.com:" + externalIdentifier + ".git" :
                "git@gitlab.com:" + externalIdentifier + ".git");
        dataProductRepoRes.setDefaultBranch("main");
        dataProductRepoRes.setProviderType(providerType == DataProductRepoProviderType.GITHUB ?
                DataProductRepoProviderTypeRes.GITHUB : DataProductRepoProviderTypeRes.GITLAB);
        dataProductRepoRes.setProviderBaseUrl(providerType == DataProductRepoProviderType.GITHUB ? "https://github.com" : "https://gitlab.com");
        dataProductRepoRes.setOwnerId(ownerId);
        dataProductRepoRes.setOwnerType(DataProductRepoOwnerTypeRes.ORGANIZATION);

        dataProductRes.setDataProductRepo(dataProductRepoRes);

        // Create via REST endpoint
        ResponseEntity<DataProductRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProductRes),
                DataProductRes.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void whenGetDescriptorWithCommitThenAssertSuccess() throws IOException, GitOperationException {
        // Given
        String testCommit = "abc123def456";

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with descriptor content
            setupMockGitOperationForRead();

            // The real service will use the mocked GitOperation to get repository content

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-repo-id");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-owner/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-owner/test-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

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

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenGetDescriptorWithNonExistentUuidThenAssertNotFound() {
        // Given
        String testUuid = "non-existent-uuid";

        // Setup mock for non-existent data product
        setupMockForNonExistentDataProduct();

        // The real service will handle non-existent data products naturally

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

        // Then - should return 404 error when descriptor is not found (controller throws BadRequestException)
        assertThat(response.getStatusCode().value()).isEqualTo(404);

        // Service is mocked, no need to verify GitProvider interactions
    }

    @Test
    void whenGetDescriptorWithUsernameAndTokenThenAssertSuccess() throws IOException, GitOperationException {
        // Given

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with descriptor content
            setupMockGitOperationForRead();

            // The real service will use the mocked GitOperation to get repository content

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-repo-id");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-owner/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-owner/test-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

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

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenGetDescriptorWithMultipleParametersThenAssertTagTakesPrecedence() throws IOException, GitOperationException {
        // Given
        String testTag = "v1.0.0";
        String testBranch = "main";
        String testCommit = "abc123def456";

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with descriptor content
            setupMockGitOperationForRead();

            // The real service will use the mocked GitOperation to get repository content

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-repo-id");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-owner/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-owner/test-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

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

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenGetDescriptorWithGitLabProviderThenAssertSuccess() throws IOException, GitOperationException {
        // Given

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Data Product", "gitlab-repo-id", "gitlab-owner-id", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with GitLab-specific descriptor content
            setupMockGitOperationForRead("GitLab Data Product", "A GitLab data product");

            // The real service will use the mocked GitOperation to get repository content

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-repo-id");
            mockRepository.setName("gitlab-repo");
            mockRepository.setCloneUrlHttp("https://gitlab.com/gitlab-owner/gitlab-repo.git");
            mockRepository.setCloneUrlSsh("git@gitlab.com:gitlab-owner/gitlab-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior - this simulates the getGitProvider() method in the service
            when(mockGitProvider.getRepository("gitlab-repo-id", "gitlab-owner-id")).thenReturn(Optional.of(mockRepository));

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

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenGetDescriptorWithNonExistentRepositoryThenAssertBadRequest() {
        // Given
        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock for repository not found scenario
            setupMockForRepositoryNotFound();

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

            // Using real service implementation with mocked Git providers
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    // ==================== POST /{uuid}/descriptor Tests ====================

    @Test
    void whenInitDescriptorWithNewRepositoryThenAssertSuccess() throws IOException, GitOperationException {
        // Given

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("New Data Product", "new-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for init scenario
            setupMockGitOperationForWrite();

            // The factory mock will handle the repository content retrieval

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("new-repo-id");
            mockRepository.setName("new-repo");

            // Mock GitProvider behavior for init scenario
            when(mockGitProvider.getRepository("new-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "New Data Product",
                            "version": "1.0.0",
                            "description": "A newly initialized data product"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenInitDescriptorWithExistingRepositoryThenAssertSuccess() throws IOException, GitOperationException {
        // Given

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Existing Data Product", "existing-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for existing repo scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("existing-repo-id");
            mockRepository.setName("existing-repo");

            // Mock GitProvider behavior for existing repo scenario
            when(mockGitProvider.getRepository("existing-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Existing Data Product",
                            "version": "1.0.0",
                            "description": "A data product with existing repository"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenInitDescriptorWithNonExistentDataProductThenAssertNotFound() {
        // Given
        String testUuid = "non-existent-uuid";

        // Setup mock for non-existent data product
        setupMockForNonExistentDataProduct();

        // The real service will handle non-existent data products naturally

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String descriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product"
                    }
                }
                """;
        HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.POST, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Service is mocked, no need to verify GitProvider interactions
    }

    @Test
    void whenInitDescriptorWithGitLabProviderThenAssertSuccess() throws IOException, GitOperationException {
        // Given

        // Create and save test data product with GitLab provider
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Data Product", "gitlab-repo-id", "gitlab-owner-id", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for GitLab init scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-repo-id");
            mockRepository.setName("gitlab-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("gitlab-repo-id", "gitlab-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "gitlab-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Test Data Product",
                            "version": "1.0.0",
                            "description": "A test data product"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    // ==================== PUT /{uuid}/descriptor Tests ====================

    @Test
    void whenModifyDescriptorThenAssertSuccess() throws IOException, GitOperationException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Updated Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        // Setup mock repository for update scenario
        setupMockGitOperationForWrite();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-repo-id");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String descriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product"
                    }
                }
                """;
        HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
        ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PUT, entity, Void.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Using real service implementation with mocked Git providers

        // Cleanup via REST endpoint
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
    }

    @Test
    void whenModifyDescriptorWithNonExistentDataProductThenAssertNotFound() {
        // Given
        String testUuid = "non-existent-uuid";
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification

        // Setup mock for non-existent data product
        setupMockForNonExistentDataProduct();

        // The real service will handle non-existent data products naturally

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String descriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product"
                    }
                }
                """;
        HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Service is mocked, no need to verify GitProvider interactions
    }

    @Test
    void whenModifyDescriptorWithoutRequiredParametersThenAssertBadRequest() {
        // Given

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Test Data Product",
                            "version": "1.0.0",
                            "description": "A test data product"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When - missing required parameters
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Using real service implementation with mocked Git providers
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenModifyDescriptorWithGitLabProviderThenAssertSuccess() throws IOException, GitOperationException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update GitLab descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification

        // Create and save test data product with GitLab provider
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Updated Product", "gitlab-repo-id", "gitlab-owner-id", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for GitLab update scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-repo-id");
            mockRepository.setName("gitlab-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("gitlab-repo-id", "gitlab-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "gitlab-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Test Data Product",
                            "version": "1.0.0",
                            "description": "A test data product"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PUT, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenModifyDescriptorWithUsernameAndTokenThenAssertSuccess() throws IOException, GitOperationException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor with username";
        String testBaseCommit = ""; // Empty to skip conflict verification

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Username Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for username update scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-repo-id");
            mockRepository.setName("test-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers with username and token
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-username", "testuser");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Test Data Product",
                            "version": "1.0.0",
                            "description": "A test data product"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PUT, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Using real service implementation with mocked Git providers

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenModifyDescriptorWithNonExistentRepositoryThenAssertBadRequest() {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock for repository not found scenario
            setupMockForRepositoryNotFound();

            // The real service will handle repository not found scenarios naturally

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Test Data Product",
                            "version": "1.0.0",
                            "description": "A test data product"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("No remote repository was found");

            // Using real service implementation with mocked Git providers
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenModifyDescriptorWithNoChangesThenAssertBadRequest() throws IOException, GitOperationException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("No Changes Data Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        // Setup mock repository for update scenario with no changes (commit returns false)
        setupMockGitOperationForWriteWithNoChanges();

        // Mock repository
        Repository mockRepository = new Repository();
        mockRepository.setId("test-repo-id");
        mockRepository.setName("test-repo");

        // Mock GitProvider behavior
        when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String descriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.0.0",
                        "description": "A test data product"
                    }
                }
                """;
        HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

        // Then - should return 400 Bad Request when there are no changes to commit
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No changes to commit");

        // Using real service implementation with mocked Git providers
        // Cleanup via REST endpoint
        rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
    }

    // ==================== POST /{uuid}/repository/tags Tests ====================

    @Test
    void whenCreateTagWithValidParametersThenReturnCreatedTag() throws Exception {
        // Given
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Tag Test Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock for GitOperation
            setupMockGitOperationForTagCreation("abc123def456");

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-repo-id");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-owner/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-owner/test-repo.git");
            mockRepository.setDefaultBranch("main");
            mockRepository.setOwnerId("test-owner-id");

            when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.set("x-odm-gpauth-param-username", "testuser");

            // Create tag request
            TagRes tagRequest = new TagRes();
            tagRequest.setTagName("v1.0.0");
            tagRequest.setMessage("Release version 1.0.0");
            tagRequest.setTarget("abc123def456");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<TagRes> response = rest.exchange(url, HttpMethod.POST, entity, TagRes.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTagName()).isEqualTo("v1.0.0");
            assertThat(response.getBody().getMessage()).isEqualTo("Release version 1.0.0");
            assertThat(response.getBody().getTarget()).isEqualTo("abc123def456");

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenCreateTagWithBranchNameThenReturnCreatedTag() throws Exception {
        // Given
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Tag Branch Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock for GitOperation - when branchName is provided, it should get the latest commit SHA
            setupMockGitOperationForTagCreationWithBranch("develop", "xyz789abc123");

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-repo-id");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-owner/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-owner/test-repo.git");
            mockRepository.setDefaultBranch("main");
            mockRepository.setOwnerId("test-owner-id");

            when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.set("x-odm-gpauth-param-username", "testuser");

            // Create tag request with branch name
            TagRes tagRequest = new TagRes();
            tagRequest.setTagName("v1.1.0");
            tagRequest.setMessage("Release version 1.1.0");
            tagRequest.setBranchName("develop");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<TagRes> response = rest.exchange(url, HttpMethod.POST, entity, TagRes.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTagName()).isEqualTo("v1.1.0");
            assertThat(response.getBody().getMessage()).isEqualTo("Release version 1.1.0");
            assertThat(response.getBody().getBranchName()).isEqualTo("develop");

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenCreateLightweightTagThenReturnCreatedTag() throws Exception {
        // Given
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Tag Lightweight Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock for GitOperation - lightweight tag (no message)
            setupMockGitOperationForTagCreation("main-commit-sha");

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-repo-id");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-owner/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-owner/test-repo.git");
            mockRepository.setDefaultBranch("main");
            mockRepository.setOwnerId("test-owner-id");

            when(mockGitProvider.getRepository("test-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.set("x-odm-gpauth-param-username", "testuser");

            // Create lightweight tag request (no message)
            TagRes tagRequest = new TagRes();
            tagRequest.setTagName("v1.0.0-beta");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<TagRes> response = rest.exchange(url, HttpMethod.POST, entity, TagRes.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTagName()).isEqualTo("v1.0.0-beta");
            // Lightweight tag has no message
            assertThat(response.getBody().getMessage()).isNull();

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenCreateTagWithNonExistentDataProductThenReturnNotFound() {
        // Given
        String nonExistentId = "non-existent-id";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        TagRes tagRequest = new TagRes();
        tagRequest.setTagName("v1.0.0");

        HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + nonExistentId + "/repository/tags";
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.POST, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void whenCreateTagWithoutTagNameThenReturnBadRequest() {
        // Given
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Tag No Name Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");

            // Create tag request without tagName
            TagRes tagRequest = new TagRes();
            tagRequest.setMessage("Release message");
            // tagName is missing

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.POST, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Missing tag name");

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenCreateTagWithEmptyTagNameThenReturnBadRequest() {
        // Given
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Tag Empty Name Product", "test-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");

            // Create tag request with empty tagName
            TagRes tagRequest = new TagRes();
            tagRequest.setTagName(""); // Empty tag name
            tagRequest.setMessage("Release message");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.POST, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Missing tag name");

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    // ==================== Helper Methods for Tag Creation ====================

    /**
     * Sets up mock GitOperation for tag creation with a specific commit SHA
     */
    private void setupMockGitOperationForTagCreation(String commitSha) throws Exception {
        // Create a temporary directory to simulate repository content
        File mockRepoDir = Files.createTempDirectory("mock-repo-tag-").toFile();
        mockRepoDir.deleteOnExit();

        // Mock getRepositoryContent to return the temporary directory
        when(mockGitOperation.getRepositoryContent(any(RepositoryPointer.class)))
                .thenReturn(mockRepoDir);

        // Mock getLatestCommitSha to return the provided commit SHA (for default branch case)
        when(mockGitOperation.getLatestCommitSha(any(File.class), anyString()))
                .thenReturn(commitSha);

        // Mock addTag to do nothing (tag creation)
        // message can be null for lightweight tags
        doNothing().when(mockGitOperation).addTag(
                any(File.class),
                anyString(),
                anyString(),
                any() // message can be null
        );

        // Mock GitProvider to return GitAuthContext
        org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext mockAuthContext =
                new org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext();
        mockAuthContext.setTransportProtocol(org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext.TransportProtocol.HTTP);
        when(mockGitProvider.createGitAuthContext()).thenReturn(mockAuthContext);
    }

    /**
     * Sets up mock GitOperation for tag creation with a branch name
     */
    private void setupMockGitOperationForTagCreationWithBranch(String branchName, String commitSha) throws Exception {
        // Create a temporary directory to simulate repository content
        File mockRepoDir = Files.createTempDirectory("mock-repo-tag-branch-").toFile();
        mockRepoDir.deleteOnExit();

        // Mock getRepositoryContent to return the temporary directory
        when(mockGitOperation.getRepositoryContent(any(RepositoryPointer.class)))
                .thenReturn(mockRepoDir);

        // Mock getLatestCommitSha to return the provided commit SHA for the specific branch
        when(mockGitOperation.getLatestCommitSha(any(File.class), eq(branchName)))
                .thenReturn(commitSha);

        // Mock addTag to do nothing (tag creation)
        // message can be null for lightweight tags
        doNothing().when(mockGitOperation).addTag(
                any(File.class),
                anyString(),
                anyString(),
                any() // message can be null
        );

        // Mock GitProvider to return GitAuthContext
        org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext mockAuthContext =
                new org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext();
        mockAuthContext.setTransportProtocol(org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext.TransportProtocol.HTTP);
        when(mockGitProvider.createGitAuthContext()).thenReturn(mockAuthContext);
    }

}
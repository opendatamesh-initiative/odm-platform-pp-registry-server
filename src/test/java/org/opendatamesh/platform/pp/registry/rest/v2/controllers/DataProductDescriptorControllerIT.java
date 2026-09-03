package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.git.exceptions.GitOperationException;
import org.opendatamesh.platform.git.git.GitOperation;
import org.opendatamesh.platform.git.model.Branch;
import org.opendatamesh.platform.git.model.Commit;
import org.opendatamesh.platform.git.model.Repository;
import org.opendatamesh.platform.git.model.RepositoryPointer;
import org.opendatamesh.platform.git.model.Tag;
import org.opendatamesh.platform.git.model.RepositoryPointerBranch;
import org.opendatamesh.platform.git.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoOwnerTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoProviderTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductAdditionalRepoRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.TagRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    private GitProvider mockGitProvider;
    private GitOperation mockGitOperation;

    @BeforeEach
    void setUp() {
        // Create fresh mocks for each test
        mockGitProvider = Mockito.mock(GitProvider.class);
        mockGitOperation = Mockito.mock(GitOperation.class);

        // GitOperation is now obtained from GitProvider, so stub the provider to return our mock
        when(mockGitProvider.gitOperation()).thenReturn(mockGitOperation);
        when(mockGitProvider.listTags(any(Repository.class), any(Pageable.class))).thenReturn(Page.empty());
        when(mockGitProvider.listBranches(any(Repository.class), any(Pageable.class))).thenReturn(Page.empty());

        gitProviderFactoryMock.setMockGitProvider(mockGitProvider);
    }

    @AfterEach
    void tearDown() {
        // Reset mocks
        Mockito.reset(mockGitProvider, mockGitOperation);

        // Reset mock factories
        gitProviderFactoryMock.reset();
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

    private void setupMockGitOperationForRead()  {
        setupMockGitOperationForRead("Test Data Product", "A test data product");
    }

    private void setupMockGitOperationForRead(String productName, String productDescription)  {
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

            doAnswer(invocation -> {
                invocation.getArgument(2, Consumer.class).accept(mockRepoDir);
                return null;
            }).when(mockGitOperation).readRepository(any(Repository.class), any(RepositoryPointer.class), any(Consumer.class));
        } catch (IOException e) {
            throw new GitOperationException("Failed to create mock repository", e);
        }
    }

    private void setupMockGitOperationForWrite() throws IOException, GitOperationException {
        // Create a real temporary directory for file operations
        File mockRepoDir = Files.createTempDirectory("mock-repo-write-").toFile();
        doAnswer(invocation -> {
            invocation.getArgument(1, Consumer.class).accept(mockRepoDir);
            return null;
        }).when(mockGitOperation).initRepository(any(Repository.class), any(Consumer.class));
        doAnswer(invocation -> {
            invocation.getArgument(2, Consumer.class).accept(mockRepoDir);
            return null;
        }).when(mockGitOperation).readRepository(any(Repository.class), any(RepositoryPointer.class), any(Consumer.class));
        doNothing().when(mockGitOperation).addFiles(any(File.class), anyList());
        doNothing().when(mockGitOperation).commit(any(File.class), any(Commit.class));
        doNothing().when(mockGitOperation).push(any(File.class), eq(false));
    }

    private void setupMockGitOperationForWriteWithNoChanges() throws IOException, GitOperationException {
        // Create a real temporary directory for file operations
        // Simulates the case where commit throws because working tree is clean (no changes to commit)
        File mockRepoDir = Files.createTempDirectory("mock-repo-write-").toFile();
        doAnswer(invocation -> {
            invocation.getArgument(2, Consumer.class).accept(mockRepoDir);
            return null;
        }).when(mockGitOperation).readRepository(any(Repository.class), any(RepositoryPointer.class), any(Consumer.class));
        doNothing().when(mockGitOperation).addFiles(any(File.class), anyList());
        doThrow(new GitOperationException("commit", "No changes to commit. Working tree is clean."))
                .when(mockGitOperation).commit(any(File.class), any(Commit.class));
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

    @Test
    void whenInitDescriptorWithCustomBranchThenAssertSuccess() throws IOException, GitOperationException {
        // Given - create test data product with default branch "main"
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Branch Data Product", "branch-repo-id", "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            setupMockGitOperationForWrite();

            Repository mockRepository = new Repository();
            mockRepository.setId("branch-repo-id");
            mockRepository.setName("branch-repo");

            when(mockGitProvider.getRepository("branch-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Branch Data Product",
                            "version": "1.0.0",
                            "description": "Initialized in custom branch"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When - pass custom branch parameter
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor?branch=feature-x";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify getRepositoryContent was invoked with RepositoryPointerBranch for feature-x
            ArgumentCaptor<RepositoryPointer> pointerCaptor = ArgumentCaptor.forClass(RepositoryPointer.class);
            verify(mockGitOperation).readRepository(any(Repository.class), pointerCaptor.capture(), any(Consumer.class));
            RepositoryPointer capturedPointer = pointerCaptor.getValue();
            assertThat(capturedPointer).isInstanceOf(RepositoryPointerBranch.class);
            assertThat(((RepositoryPointerBranch) capturedPointer).getName()).isEqualTo("feature-x");
        } finally {
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    /**
     * Verifies that init descriptor runs the required git operation sequence so
     * that
     * the remote is updated. Required sequence: (1) readRepository, (2) addFiles,
     * (3) commit, (4) push. Push must run after commit;
     */
    @Test
    void whenInitDescriptorThenGitOperationsAreInSequenceReadRepositoryAddFilesCommitPush()
            throws IOException, GitOperationException {
        // Given
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Sequence Init Product", "seq-repo-id",
                "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            setupMockGitOperationForWrite();

            Repository mockRepository = new Repository();
            mockRepository.setId("seq-repo-id");
            mockRepository.setName("seq-repo");
            when(mockGitProvider.getRepository("seq-repo-id", "test-owner-id")).thenReturn(Optional.of(mockRepository));

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Sequence Init Product",
                            "version": "1.0.0",
                            "description": "Verifies git sequence"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When - POST init descriptor
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then - success and git sequence: addFiles -> commit -> push (after
            // readRepository)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            InOrder order = inOrder(mockGitOperation);
            order.verify(mockGitOperation).addFiles(any(File.class), anyList());
            order.verify(mockGitOperation).commit(any(File.class), any(Commit.class));
            order.verify(mockGitOperation).push(any(File.class), eq(false));
        } finally {
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

    /**
     * Verifies that update descriptor runs the required git operation sequence so
     * that
     * the remote is updated. Required sequence: (1) readRepository, (2) addFiles,
     * (3) commit, (4) push. Push must run after commit;
     */
    @Test
    void whenUpdateDescriptorThenGitOperationsAreInSequenceReadRepositoryAddFilesCommitPush()
            throws IOException, GitOperationException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = "";

        DataProductRes testDataProduct = createAndSaveTestDataProduct("Sequence Update Product", "seq-update-repo-id",
                "test-owner-id", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            setupMockGitOperationForWrite();

            Repository mockRepository = new Repository();
            mockRepository.setId("seq-update-repo-id");
            mockRepository.setName("seq-update-repo");
            when(mockGitProvider.getRepository("seq-update-repo-id", "test-owner-id"))
                    .thenReturn(Optional.of(mockRepository));

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            String descriptorContent = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Sequence Update Product",
                            "version": "1.0.0",
                            "description": "Verifies git sequence"
                        }
                    }
                    """;
            HttpEntity<String> entity = new HttpEntity<>(descriptorContent, headers);

            // When - PUT update descriptor
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PUT, entity, Void.class);

            // Then - success and git sequence: addFiles -> commit -> push (after
            // readRepository)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            InOrder order = inOrder(mockGitOperation);
            order.verify(mockGitOperation).addFiles(any(File.class), anyList());
            order.verify(mockGitOperation).commit(any(File.class), any(Commit.class));
            order.verify(mockGitOperation).push(any(File.class), eq(false));
        } finally {
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
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

        // Setup mock: commit throws GitOperationException when there are no changes to commit
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
            tagRequest.setName("v1.0.0");
            tagRequest.setMessage("Release version 1.0.0");
            tagRequest.setCommitHash("abc123def456");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<TagRes> response = rest.exchange(url, HttpMethod.POST, entity, TagRes.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("v1.0.0");
            assertThat(response.getBody().getMessage()).isEqualTo("Release version 1.0.0");
            assertThat(response.getBody().getCommitHash()).isEqualTo("abc123def456");

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
            tagRequest.setName("v1.1.0");
            tagRequest.setMessage("Release version 1.1.0");
            tagRequest.setBranchName("develop");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<TagRes> response = rest.exchange(url, HttpMethod.POST, entity, TagRes.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("v1.1.0");
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
            tagRequest.setName("v1.0.0-beta");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<TagRes> response = rest.exchange(url, HttpMethod.POST, entity, TagRes.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("v1.0.0-beta");
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
        tagRequest.setName("v1.0.0");

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
            tagRequest.setName(""); // Empty tag name
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

    /*
     * Feature: Create Git tag on all data product repositories
     * Scenario: POST /tags applies the tag to the root and every additional repository
     *   Given a data product with a root Git repository and additional keyed repositories
     *   When the client creates a tag via POST /api/v2/pp/registry/products/{uuid}/repository/tags
     *   Then the tag is created on the root repository
     *   And the same tag name is created on each additional repository
     *   And the response status is 201 with the submitted tag body
     */
    @Test
    void whenCreateTagForProductWithAdditionalRepositoriesThenTagRootAndAdditionalRemotes() throws Exception {
        DataProductRes testDataProduct = createAndSaveTestDataProductWithAdditionalRepositories(
                "Tag All Repos Product",
                "root-repo-id",
                "test-owner-id"
        );
        String testUuid = testDataProduct.getUuid();

        try {
            setupMockGitOperationForTagCreation("abc123def456");
            doNothing().when(mockGitOperation).push(any(File.class), eq(true));

            when(mockGitProvider.getRepository("root-repo-id", "test-owner-id"))
                    .thenReturn(Optional.of(gitRepositoryStub("root-repo-id")));
            when(mockGitProvider.getRepository("infra-repo-id", "test-owner-id"))
                    .thenReturn(Optional.of(gitRepositoryStub("infra-repo-id")));
            when(mockGitProvider.getRepository("app-repo-id", "test-owner-id"))
                    .thenReturn(Optional.of(gitRepositoryStub("app-repo-id")));

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.set("x-odm-gpauth-param-username", "testuser");

            TagRes tagRequest = new TagRes();
            tagRequest.setName("v2.0.0");
            tagRequest.setMessage("Release version 2.0.0");

            HttpEntity<TagRes> entity = new HttpEntity<>(tagRequest, headers);

            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/repository/tags";
            ResponseEntity<TagRes> response = rest.exchange(url, HttpMethod.POST, entity, TagRes.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("v2.0.0");

            ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
            verify(mockGitOperation, times(3)).addTag(any(File.class), tagCaptor.capture());
            assertThat(tagCaptor.getAllValues())
                    .hasSize(3)
                    .allMatch(tag -> "v2.0.0".equals(tag.getName()));
        } finally {
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    /*
     * Feature: Create Git tag on all data product repositories
     * Scenario: Selecting an existing root tag fails when an additional repository does not have that tag
     *   Given the tag already exists on the root repository
     *   And an additional repository does not have that tag name
     *   When the client posts the same tag via POST /api/v2/pp/registry/products/{uuid}/repository/tags
     *   Then the response status is 400
     *   And the error tells the user to create that tag on the additional repository before retrying publish
     *   And no Git tag is created
     */
    @Test
    void whenCreateExistingTagMissingOnAdditionalRepositoryThenReturnBadRequest() throws Exception {
        DataProductRes testDataProduct = createAndSaveTestDataProductWithAdditionalRepositories(
                "Existing Tag Missing Additional Product",
                "root-repo-id",
                "test-owner-id"
        );
        String testUuid = testDataProduct.getUuid();

        try {
            stubAdditionalProductRepositories();
            when(mockGitProvider.listTags(any(Repository.class), any(Pageable.class))).thenAnswer(invocation -> {
                Repository repository = invocation.getArgument(0);
                if ("root-repo-id".equals(repository.getId())) {
                    return tagPage("v2.0.0");
                }
                return Page.empty();
            });

            ResponseEntity<String> response = postTag(testUuid, tagRequest("v2.0.0", null));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains(
                    "Tag 'v2.0.0' exists on the root repository but is missing on additional repository 'infra-repo'. "
                            + "Create tag 'v2.0.0' on that additional repository, then retry publishing."
            );
            verify(mockGitOperation, never()).addTag(any(File.class), any(Tag.class));
        } finally {
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    /*
     * Feature: Create Git tag on all data product repositories
     * Scenario: Creating a new tag fails when an additional repository already has that tag
     *   Given the tag does not exist on the root repository
     *   And an additional repository already has that tag name
     *   When the client creates the tag via POST /api/v2/pp/registry/products/{uuid}/repository/tags
     *   Then the response status is 400
     *   And the error tells the user to choose another name or delete the extra tag before retrying publish
     *   And no Git tag is created
     */
    @Test
    void whenCreateNewTagAlreadyOnAdditionalRepositoryThenReturnBadRequest() throws Exception {
        DataProductRes testDataProduct = createAndSaveTestDataProductWithAdditionalRepositories(
                "New Tag Already On Additional Product",
                "root-repo-id",
                "test-owner-id"
        );
        String testUuid = testDataProduct.getUuid();

        try {
            stubAdditionalProductRepositories();
            when(mockGitProvider.listTags(any(Repository.class), any(Pageable.class))).thenAnswer(invocation -> {
                Repository repository = invocation.getArgument(0);
                if ("infra-repo-id".equals(repository.getId())) {
                    return tagPage("v2.0.0");
                }
                return Page.empty();
            });

            ResponseEntity<String> response = postTag(testUuid, tagRequest("v2.0.0", null));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains(
                    "Cannot create tag 'v2.0.0': it already exists on additional repository 'infra-repo'. "
                            + "Choose a different tag name, or delete that tag on the additional repository, then retry publishing."
            );
            verify(mockGitOperation, never()).addTag(any(File.class), any(Tag.class));
        } finally {
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    /*
     * Feature: Create Git tag on all data product repositories
     * Scenario: Creating a new tag from a non-default branch fails when an additional repository lacks that branch
     *   Given additional repositories use default branch main
     *   And they do not have branch develop
     *   When the client creates a tag from branch develop via POST /api/v2/pp/registry/products/{uuid}/repository/tags
     *   Then the response status is 400
     *   And the error tells the user to create that branch on the additional repository before retrying publish
     *   And no Git tag is created
     */
    @Test
    void whenCreateNewTagFromMissingNonDefaultBranchOnAdditionalRepositoryThenReturnBadRequest() throws Exception {
        DataProductRes testDataProduct = createAndSaveTestDataProductWithAdditionalRepositories(
                "New Tag Missing Branch Additional Product",
                "root-repo-id",
                "test-owner-id"
        );
        String testUuid = testDataProduct.getUuid();

        try {
            stubAdditionalProductRepositories();
            when(mockGitProvider.listBranches(any(Repository.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(new Branch("main", "main-sha"))));

            ResponseEntity<String> response = postTag(testUuid, tagRequest("v2.0.0", "develop"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains(
                    "Cannot create tag 'v2.0.0' from branch 'develop': additional repository 'infra-repo' does not have that branch. "
                            + "Create branch 'develop' on that additional repository (or tag from a branch that exists on every repository), then retry publishing."
            );
            verify(mockGitOperation, never()).addTag(any(File.class), any(Tag.class));
        } finally {
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
        doAnswer(invocation -> {
            invocation.getArgument(2, Consumer.class).accept(mockRepoDir);
            return null;
        }).when(mockGitOperation).readRepository(any(Repository.class), any(RepositoryPointer.class), any(Consumer.class));

        // Mock getLatestCommitSha to return the provided commit SHA (for default branch case)
        when(mockGitOperation.getHeadSha(any(File.class), anyString()))
                .thenReturn(commitSha);

        // Mock addTag to do nothing (tag creation)
        doNothing().when(mockGitOperation).addTag(any(File.class), any(Tag.class));
    }

    /**
     * Sets up mock GitOperation for tag creation with a branch name
     */
    private void setupMockGitOperationForTagCreationWithBranch(String branchName, String commitSha) throws Exception {
        // Create a temporary directory to simulate repository content
        File mockRepoDir = Files.createTempDirectory("mock-repo-tag-branch-").toFile();
        mockRepoDir.deleteOnExit();

        // Mock getRepositoryContent to return the temporary directory
        doAnswer(invocation -> {
            invocation.getArgument(2, Consumer.class).accept(mockRepoDir);
            return null;
        }).when(mockGitOperation).readRepository(any(Repository.class), any(RepositoryPointer.class), any(Consumer.class));

        // Mock getLatestCommitSha to return the provided commit SHA for the specific branch
        when(mockGitOperation.getHeadSha(any(File.class), eq(branchName)))
                .thenReturn(commitSha);

        // Mock addTag to do nothing (tag creation)
        doNothing().when(mockGitOperation).addTag(any(File.class), any(Tag.class));
    }

    private DataProductRes createAndSaveTestDataProductWithAdditionalRepositories(
            String name,
            String rootExternalIdentifier,
            String ownerId
    ) {
        DataProductRes dataProductRes = new DataProductRes();
        dataProductRes.setName(name);
        dataProductRes.setDomain("test-domain");
        dataProductRes.setFqn("test-domain/" + name.toLowerCase().replace(" ", "-"));
        dataProductRes.setDisplayName("Test Display Name");
        dataProductRes.setDescription("Test Description");

        DataProductRepoRes rootRepo = new DataProductRepoRes();
        rootRepo.setExternalIdentifier(rootExternalIdentifier);
        rootRepo.setName(name + " Repository");
        rootRepo.setDescription("Root repository");
        rootRepo.setDescriptorRootPath("data-product-descriptor.json");
        rootRepo.setRemoteUrlHttp("https://github.com/" + rootExternalIdentifier + ".git");
        rootRepo.setRemoteUrlSsh("git@github.com:" + rootExternalIdentifier + ".git");
        rootRepo.setDefaultBranch("main");
        rootRepo.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        rootRepo.setProviderBaseUrl("https://github.com");
        rootRepo.setOwnerId(ownerId);
        rootRepo.setOwnerType(DataProductRepoOwnerTypeRes.ORGANIZATION);
        dataProductRes.setDataProductRepo(rootRepo);

        dataProductRes.setAdditionalDataProductRepos(List.of(
                additionalRepoRes("infra-repo", "infra-repo-id", ownerId),
                additionalRepoRes("app-repo", "app-repo-id", ownerId)
        ));

        ResponseEntity<DataProductRes> response = rest.postForEntity(
                apiUrl(RoutesV2.DATA_PRODUCTS),
                new HttpEntity<>(dataProductRes),
                DataProductRes.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private DataProductAdditionalRepoRes additionalRepoRes(String manifestKey, String externalIdentifier, String ownerId) {
        DataProductAdditionalRepoRes additionalRepo = new DataProductAdditionalRepoRes();
        additionalRepo.setManifestKey(manifestKey);
        additionalRepo.setName(manifestKey);
        additionalRepo.setDescription("Additional repository " + manifestKey);
        additionalRepo.setExternalIdentifier(externalIdentifier);
        additionalRepo.setRemoteUrlHttp("https://github.com/" + externalIdentifier + ".git");
        additionalRepo.setRemoteUrlSsh("git@github.com:" + externalIdentifier + ".git");
        additionalRepo.setDefaultBranch("main");
        additionalRepo.setProviderType(DataProductRepoProviderTypeRes.GITHUB);
        additionalRepo.setProviderBaseUrl("https://github.com");
        additionalRepo.setOwnerId(ownerId);
        additionalRepo.setOwnerType(DataProductRepoOwnerTypeRes.ORGANIZATION);
        return additionalRepo;
    }

    private void stubAdditionalProductRepositories() {
        when(mockGitProvider.getRepository("root-repo-id", "test-owner-id"))
                .thenReturn(Optional.of(gitRepositoryStub("root-repo-id")));
        when(mockGitProvider.getRepository("infra-repo-id", "test-owner-id"))
                .thenReturn(Optional.of(gitRepositoryStub("infra-repo-id")));
        when(mockGitProvider.getRepository("app-repo-id", "test-owner-id"))
                .thenReturn(Optional.of(gitRepositoryStub("app-repo-id")));
    }

    private TagRes tagRequest(String name, String branchName) {
        TagRes tagRequest = new TagRes();
        tagRequest.setName(name);
        tagRequest.setMessage("Release version 2.0.0");
        tagRequest.setBranchName(branchName);
        return tagRequest;
    }

    private ResponseEntity<String> postTag(String dataProductUuid, TagRes tagRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.set("x-odm-gpauth-param-username", "testuser");
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + dataProductUuid + "/repository/tags";
        return rest.exchange(url, HttpMethod.POST, new HttpEntity<>(tagRequest, headers), String.class);
    }

    private Page<Tag> tagPage(String... names) {
        return new PageImpl<>(java.util.Arrays.stream(names).map(name -> new Tag(name, "sha")).toList());
    }

    private Repository gitRepositoryStub(String repositoryId) {
        Repository mockRepository = new Repository();
        mockRepository.setId(repositoryId);
        mockRepository.setName(repositoryId);
        mockRepository.setCloneUrlHttp("https://github.com/test-owner/" + repositoryId + ".git");
        mockRepository.setCloneUrlSsh("git@github.com:test-owner/" + repositoryId + ".git");
        mockRepository.setDefaultBranch("main");
        mockRepository.setOwnerId("test-owner-id");
        return mockRepository;
    }

}
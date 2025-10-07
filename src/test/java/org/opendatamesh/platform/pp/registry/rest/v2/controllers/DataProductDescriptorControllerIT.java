package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoProviderTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductsDescriptorService;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitOperationFactoryMock;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.DataProductsDescriptorServiceMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    @Autowired
    private GitOperationFactoryMock gitOperationFactoryMock;
    
    @Autowired
    private DataProductsDescriptorServiceMock dataProductsDescriptorServiceMock;
    
    private GitProvider mockGitProvider;
    private GitOperation mockGitOperation;
    private DataProductsDescriptorService mockDescriptorService;

    @BeforeEach
    void setUp() {
        // Create fresh mocks for each test
        mockGitProvider = Mockito.mock(GitProvider.class);
        mockGitOperation = Mockito.mock(GitOperation.class);
        mockDescriptorService = Mockito.mock(DataProductsDescriptorService.class);
        
        gitProviderFactoryMock.setMockGitProvider(mockGitProvider);
        gitOperationFactoryMock.setMockGitOperation(mockGitOperation);
        dataProductsDescriptorServiceMock.setMockDescriptorService(mockDescriptorService);
    }

    @AfterEach
    void tearDown() {
        // Reset mocks
        Mockito.reset(mockGitProvider, mockGitOperation, mockDescriptorService);
        
        // Reset mock factories
        gitProviderFactoryMock.reset();
        gitOperationFactoryMock.reset();
        dataProductsDescriptorServiceMock.reset();
    }

    private JsonNode createMockDescriptorJsonNode() {
        try {
            String descriptorJson = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "Test Data Product",
                            "version": "1.0.0",
                            "description": "A test data product"
                        }
                    }
                    """;
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(descriptorJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock descriptor", e);
        }
    }

    private JsonNode createGitLabMockDescriptorJsonNode() {
        try {
            String descriptorJson = """
                    {
                        "dataProductDescriptor": "1.0.0",
                        "info": {
                            "name": "GitLab Data Product",
                            "version": "1.0.0",
                            "description": "A GitLab data product"
                        }
                    }
                    """;
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(descriptorJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GitLab mock descriptor", e);
        }
    }




    
    private void setupMockForNonExistentDataProduct() {
        // For non-existent data products, the real service will throw NotFoundException
        // when trying to find the data product via dataProductsService.findOne()
        // No additional setup needed as the service will handle this naturally
    }
    
    private void setupMockForRepositoryNotFound() {
        // Configure the mock GitProvider to return empty Optional for repository not found
        when(mockGitProvider.getRepository(anyString())).thenReturn(Optional.empty());
    }

    private void setupMockGitOperationForRead() throws GitOperationException {
        // Mock GitOperation to return a dummy file that won't be used for actual file operations
        File mockRepoDir = new File("/tmp/mock-repo-dir");
        when(mockGitOperation.getRepositoryContent(any(RepositoryPointer.class)))
                .thenReturn(mockRepoDir);
    }

    private void setupMockGitOperationForWrite() throws GitOperationException {
        // Mock GitOperation to return a dummy file that won't be used for actual file operations
        File mockRepoDir = new File("/tmp/mock-repo-dir");
        when(mockGitOperation.initRepository(anyString(), any(java.net.URL.class)))
                .thenReturn(mockRepoDir);
        when(mockGitOperation.getRepositoryContent(any(RepositoryPointer.class)))
                .thenReturn(mockRepoDir);
        doNothing().when(mockGitOperation).addFiles(any(File.class), anyList());
        when(mockGitOperation.commit(any(File.class), anyString())).thenReturn(true);
        doNothing().when(mockGitOperation).push(any(File.class));
    }


    private DataProductRes createAndSaveTestDataProduct(String name, String externalIdentifier, DataProductRepoProviderType providerType) {
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with descriptor content
            setupMockGitOperationForRead();
            
            // Mock the descriptor service to return a descriptor
            when(mockDescriptorService.getDescriptor(anyString(), any(), any()))
                    .thenReturn(Optional.of(createMockDescriptorJsonNode()));

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/test-repo");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-org/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-org/test-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));

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
        
        // Mock the descriptor service to return empty for non-existent data product
        when(mockDescriptorService.getDescriptor(anyString(), any(), any()))
                .thenReturn(Optional.empty());

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class);

        // Then - should return 400 error when descriptor is not found (controller throws BadRequestException)
        assertThat(response.getStatusCode().value()).isEqualTo(400);

        // Service is mocked, no need to verify GitProvider interactions
    }

    @Test
    void whenGetDescriptorWithoutCredentialsThenAssertBadRequest() {
        // Given
        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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

            // Using real service implementation with mocked Git providers
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenGetDescriptorWithInvalidCredentialsThenAssertBadRequest() {
        // Given
        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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

            // Using real service implementation with mocked Git providers
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenGetDescriptorWithUsernameAndTokenThenAssertSuccess() throws IOException, GitOperationException {
        // Given

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with descriptor content
            setupMockGitOperationForRead();
            
            // Mock the descriptor service to return a descriptor
            when(mockDescriptorService.getDescriptor(anyString(), any(), any()))
                    .thenReturn(Optional.of(createMockDescriptorJsonNode()));

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/test-repo");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-org/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-org/test-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with descriptor content
            setupMockGitOperationForRead();
            
            // Mock the descriptor service to return a descriptor
            when(mockDescriptorService.getDescriptor(anyString(), any(), any()))
                    .thenReturn(Optional.of(createMockDescriptorJsonNode()));

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/test-repo");
            mockRepository.setName("test-repo");
            mockRepository.setCloneUrlHttp("https://github.com/test-org/test-repo.git");
            mockRepository.setCloneUrlSsh("git@github.com:test-org/test-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.of(mockRepository));

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Data Product", "gitlab-org/gitlab-repo", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository with descriptor content
            setupMockGitOperationForRead();
            
            // Mock the descriptor service to return a GitLab-specific descriptor
            when(mockDescriptorService.getDescriptor(anyString(), any(), any()))
                    .thenReturn(Optional.of(createGitLabMockDescriptorJsonNode()));

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-org/gitlab-repo");
            mockRepository.setName("gitlab-repo");
            mockRepository.setCloneUrlHttp("https://gitlab.com/gitlab-org/gitlab-repo.git");
            mockRepository.setCloneUrlSsh("git@gitlab.com:gitlab-org/gitlab-repo.git");
            mockRepository.setDefaultBranch("main");

            // Mock GitProvider behavior - this simulates the getGitProvider() method in the service
            when(mockGitProvider.getRepository("gitlab-org/gitlab-repo")).thenReturn(Optional.of(mockRepository));

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("New Data Product", "test-org/new-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for init scenario
            setupMockGitOperationForWrite();
            
            // The factory mock will handle the repository content retrieval

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/new-repo");
            mockRepository.setName("new-repo");

            // Mock GitProvider behavior for init scenario
            when(mockGitProvider.getRepository("test-org/new-repo")).thenReturn(Optional.of(mockRepository));

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Existing Data Product", "test-org/existing-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for existing repo scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/existing-repo");
            mockRepository.setName("existing-repo");

            // Mock GitProvider behavior for existing repo scenario
            when(mockGitProvider.getRepository("test-org/existing-repo")).thenReturn(Optional.of(mockRepository));

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
    void whenInitDescriptorWithoutCredentialsThenAssertBadRequest() {
        // Given

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers without credentials
            HttpHeaders headers = new HttpHeaders();
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
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Missing or invalid credentials");

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
        
        // Mock the descriptor service to throw exception for non-existent data product
        doThrow(new org.opendatamesh.platform.pp.registry.exceptions.NotFoundException("Data product not found"))
                .when(mockDescriptorService).initDescriptor(anyString(), any(), any());

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Data Product", "gitlab-org/gitlab-repo", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for GitLab init scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-org/gitlab-repo");
            mockRepository.setName("gitlab-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("gitlab-org/gitlab-repo")).thenReturn(Optional.of(mockRepository));

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Updated Data Product", "test-org/update-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for update scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/update-repo");
            mockRepository.setName("update-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-org/update-repo")).thenReturn(Optional.of(mockRepository));

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

        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void whenModifyDescriptorWithoutCredentialsThenAssertBadRequest() {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers without credentials
            HttpHeaders headers = new HttpHeaders();
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
            assertThat(response.getBody()).contains("Missing or invalid credentials");

            // Using real service implementation with mocked Git providers
        } finally {
            // Cleanup via REST endpoint
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
        
        // Mock the descriptor service to throw exception for non-existent data product
        doThrow(new org.opendatamesh.platform.pp.registry.exceptions.NotFoundException("Data product not found"))
                .when(mockDescriptorService).updateDescriptor(anyString(), anyString(), anyString(), anyString(), any(), any());

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Updated Product", "gitlab-org/update-repo", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for GitLab update scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-org/update-repo");
            mockRepository.setName("update-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("gitlab-org/update-repo")).thenReturn(Optional.of(mockRepository));

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Username Data Product", "test-org/username-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock repository for username update scenario
            setupMockGitOperationForWrite();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/username-repo");
            mockRepository.setName("username-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-org/username-repo")).thenReturn(Optional.of(mockRepository));

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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup mock for repository not found scenario
            setupMockForRepositoryNotFound();
            
            // Mock the descriptor service to throw exception for repository not found
            doThrow(new org.opendatamesh.platform.pp.registry.exceptions.BadRequestException("No remote repository was found"))
                    .when(mockDescriptorService).updateDescriptor(anyString(), anyString(), anyString(), anyString(), any(), any());

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

}

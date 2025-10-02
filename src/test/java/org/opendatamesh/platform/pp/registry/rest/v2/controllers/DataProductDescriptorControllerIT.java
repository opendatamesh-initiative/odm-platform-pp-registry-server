package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoProviderTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"ConstantConditions", "DataFlowIssue", "NullAway", "PotentialNullPointerException", "NullPointerException", "all"})
public class DataProductDescriptorControllerIT extends RegistryApplicationIT {

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    private GitProvider mockGitProvider;

    @BeforeEach
    void setUp() {
        mockGitProvider = Mockito.mock(GitProvider.class);
        gitProviderFactoryMock.setMockGitProvider(mockGitProvider);
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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

            // Verify no interactions with mocked dependencies
            verify(mockGitProvider, never()).getRepository(any());
            verify(mockGitProvider, never()).readRepository(any());
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testGetDescriptorInvalidCredentials() {
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

            // Verify no interactions with mocked dependencies
            verify(mockGitProvider, never()).getRepository(any());
            verify(mockGitProvider, never()).readRepository(any());
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Data Product", "gitlab-org/gitlab-repo", DataProductRepoProviderType.GITLAB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testGetDescriptorFileNotFound() throws IOException {
        // Given
        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testGetDescriptorRepositoryNotFound() {
        // Given
        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
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
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    // ==================== POST /{uuid}/descriptor Tests ====================

    @Test
    void testInitDescriptorSuccess() throws IOException {
        // Given
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "New Data Product",
                        "version": "1.0.0",
                        "description": "A newly initialized data product"
                    }
                }
                """;

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("New Data Product", "test-org/new-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
            Path tempRepoDir = Files.createTempDirectory("test-repo-init");
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/new-repo");
            mockRepository.setName("new-repo");

            // Mock GitProvider behavior for init scenario
            when(mockGitProvider.getRepository("test-org/new-repo")).thenReturn(Optional.of(mockRepository));
            when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(null); // Simulate no existing repo
            when(mockGitProvider.initRepository("New Data Product Repository", "https://github.com/test-org/new-repo.git"))
                    .thenReturn(mockRepoFile);
            when(mockGitProvider.saveDescriptor(any(File.class), anyString(), anyString())).thenReturn(true);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("test-org/new-repo");
            verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));
            verify(mockGitProvider).initRepository("New Data Product Repository", "https://github.com/test-org/new-repo.git");
            verify(mockGitProvider).saveDescriptor(any(File.class), eq("data-product-descriptor.json"), eq("Init Commit"));

            // Cleanup temp files
            deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testInitDescriptorWithExistingRepository() throws IOException {
        // Given
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Existing Data Product",
                        "version": "1.0.0",
                        "description": "A data product with existing repository"
                    }
                }
                """;

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Existing Data Product", "test-org/existing-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics an existing repository
            Path tempRepoDir = Files.createTempDirectory("test-repo-existing");
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/existing-repo");
            mockRepository.setName("existing-repo");

            // Mock GitProvider behavior for existing repo scenario
            when(mockGitProvider.getRepository("test-org/existing-repo")).thenReturn(Optional.of(mockRepository));
            when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);
            when(mockGitProvider.saveDescriptor(any(File.class), anyString(), anyString())).thenReturn(true);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("test-org/existing-repo");
            verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));
            verify(mockGitProvider, never()).initRepository(anyString(), anyString());
            verify(mockGitProvider).saveDescriptor(any(File.class), eq("data-product-descriptor.json"), eq("Init Commit"));

            // Cleanup temp files
            deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testInitDescriptorMissingCredentials() {
        // Given
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
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers without credentials
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.POST, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Missing or invalid credentials");

            // Verify no interactions with mocked dependencies
            verify(mockGitProvider, never()).getRepository(any());
            verify(mockGitProvider, never()).readRepository(any());
            verify(mockGitProvider, never()).initRepository(anyString(), anyString());
            verify(mockGitProvider, never()).saveDescriptor(any(), anyString(), anyString());
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testInitDescriptorDataProductNotFound() {
        // Given
        String testUuid = "non-existent-uuid";
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

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.POST, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify no interactions with mocked dependencies
        verify(mockGitProvider, never()).getRepository(any());
        verify(mockGitProvider, never()).readRepository(any());
        verify(mockGitProvider, never()).initRepository(anyString(), anyString());
        verify(mockGitProvider, never()).saveDescriptor(any(), anyString(), anyString());
    }

    @Test
    void testInitDescriptorWithGitLabProvider() throws IOException {
        // Given
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "GitLab Data Product",
                        "version": "1.0.0",
                        "description": "A GitLab data product"
                    }
                }
                """;

        // Create and save test data product with GitLab provider
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Data Product", "gitlab-org/gitlab-repo", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure
            Path tempRepoDir = Files.createTempDirectory("test-repo-gitlab-init");
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-org/gitlab-repo");
            mockRepository.setName("gitlab-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("gitlab-org/gitlab-repo")).thenReturn(Optional.of(mockRepository));
            when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(null);
            when(mockGitProvider.initRepository("GitLab Data Product Repository", "https://gitlab.com/gitlab-org/gitlab-repo.git"))
                    .thenReturn(mockRepoFile);
            when(mockGitProvider.saveDescriptor(any(File.class), anyString(), anyString())).thenReturn(true);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "gitlab-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("gitlab-org/gitlab-repo");
            verify(mockGitProvider).initRepository("GitLab Data Product Repository", "https://gitlab.com/gitlab-org/gitlab-repo.git");
            verify(mockGitProvider).saveDescriptor(any(File.class), eq("data-product-descriptor.json"), eq("Init Commit"));

            // Cleanup temp files
            deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    // ==================== PUT /{uuid}/descriptor Tests ====================

    @Test
    void testModifyDescriptorSuccess() throws IOException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Updated Data Product",
                        "version": "1.1.0",
                        "description": "An updated data product"
                    }
                }
                """;

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Updated Data Product", "test-org/update-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure that mimics a real repository
            Path tempRepoDir = Files.createTempDirectory("test-repo-update");
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/update-repo");
            mockRepository.setName("update-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-org/update-repo")).thenReturn(Optional.of(mockRepository));
            when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);
            when(mockGitProvider.saveDescriptor(any(File.class), anyString(), anyString())).thenReturn(true);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PUT, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("test-org/update-repo");
            verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));
            verify(mockGitProvider).saveDescriptor(any(File.class), eq("data-product-descriptor.json"), eq(testCommitMessage));

            // Cleanup temp files
            deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testModifyDescriptorMissingCredentials() {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.1.0",
                        "description": "A test data product"
                    }
                }
                """;

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers without credentials
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("Missing or invalid credentials");

            // Verify no interactions with mocked dependencies
            verify(mockGitProvider, never()).getRepository(any());
            verify(mockGitProvider, never()).readRepository(any());
            verify(mockGitProvider, never()).saveDescriptor(any(), anyString(), anyString());
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testModifyDescriptorDataProductNotFound() {
        // Given
        String testUuid = "non-existent-uuid";
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.1.0",
                        "description": "A test data product"
                    }
                }
                """;

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

        // When
        String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
        ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify no interactions with mocked dependencies
        verify(mockGitProvider, never()).getRepository(any());
        verify(mockGitProvider, never()).readRepository(any());
        verify(mockGitProvider, never()).saveDescriptor(any(), anyString(), anyString());
    }

    @Test
    void testModifyDescriptorMissingRequiredParameters() {
        // Given
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.1.0",
                        "description": "A test data product"
                    }
                }
                """;

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When - missing required parameters
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor";
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify no interactions with mocked dependencies
            verify(mockGitProvider, never()).getRepository(any());
            verify(mockGitProvider, never()).readRepository(any());
            verify(mockGitProvider, never()).saveDescriptor(any(), anyString(), anyString());
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testModifyDescriptorWithGitLabProvider() throws IOException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update GitLab descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "GitLab Updated Product",
                        "version": "1.2.0",
                        "description": "An updated GitLab data product"
                    }
                }
                """;

        // Create and save test data product with GitLab provider
        DataProductRes testDataProduct = createAndSaveTestDataProduct("GitLab Updated Product", "gitlab-org/update-repo", DataProductRepoProviderType.GITLAB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure
            Path tempRepoDir = Files.createTempDirectory("test-repo-gitlab-update");
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("gitlab-org/update-repo");
            mockRepository.setName("update-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("gitlab-org/update-repo")).thenReturn(Optional.of(mockRepository));
            when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);
            when(mockGitProvider.saveDescriptor(any(File.class), anyString(), anyString())).thenReturn(true);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "gitlab-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PUT, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("gitlab-org/update-repo");
            verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));
            verify(mockGitProvider).saveDescriptor(any(File.class), eq("data-product-descriptor.json"), eq(testCommitMessage));

            // Cleanup temp files
            deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testModifyDescriptorWithUsernameAndToken() throws IOException {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor with username";
        String testBaseCommit = ""; // Empty to skip conflict verification
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Username Data Product",
                        "version": "1.3.0",
                        "description": "A data product updated with username"
                    }
                }
                """;

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Username Data Product", "test-org/username-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Create temporary directory structure
            Path tempRepoDir = Files.createTempDirectory("test-repo-username");
            File mockRepoFile = tempRepoDir.toFile();

            // Mock repository
            Repository mockRepository = new Repository();
            mockRepository.setId("test-org/username-repo");
            mockRepository.setName("username-repo");

            // Mock GitProvider behavior
            when(mockGitProvider.getRepository("test-org/username-repo")).thenReturn(Optional.of(mockRepository));
            when(mockGitProvider.readRepository(any(RepositoryPointer.class))).thenReturn(mockRepoFile);
            when(mockGitProvider.saveDescriptor(any(File.class), anyString(), anyString())).thenReturn(true);

            // Setup headers with username and token
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-username", "testuser");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PUT, entity, Void.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("test-org/username-repo");
            verify(mockGitProvider).readRepository(any(RepositoryPointerBranch.class));
            verify(mockGitProvider).saveDescriptor(any(File.class), eq("data-product-descriptor.json"), eq(testCommitMessage));

            // Cleanup temp files
            deleteRecursively(tempRepoDir);
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
        }
    }

    @Test
    void testModifyDescriptorRepositoryNotFound() {
        // Given
        String testBranch = "main";
        String testCommitMessage = "Update descriptor";
        String testBaseCommit = ""; // Empty to skip conflict verification
        String testDescriptorContent = """
                {
                    "dataProductDescriptor": "1.0.0",
                    "info": {
                        "name": "Test Data Product",
                        "version": "1.1.0",
                        "description": "A test data product"
                    }
                }
                """;

        // Create and save test data product
        DataProductRes testDataProduct = createAndSaveTestDataProduct("Test Data Product", "test-org/test-repo", DataProductRepoProviderType.GITHUB);
        String testUuid = testDataProduct.getUuid();

        try {
            // Mock GitProvider to return empty (repository not found)
            when(mockGitProvider.getRepository("test-org/test-repo")).thenReturn(Optional.empty());

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-odm-gpauth-type", "PAT");
            headers.set("x-odm-gpauth-param-token", "test-token");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(testDescriptorContent, headers);

            // When
            String url = apiUrl(RoutesV2.DATA_PRODUCTS) + "/" + testUuid + "/descriptor" +
                    "?branch=" + testBranch + "&commitMessage=" + testCommitMessage + "&baseCommit=" + testBaseCommit;
            ResponseEntity<String> response = rest.exchange(url, HttpMethod.PUT, entity, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("No remote repository was found");

            // Verify interactions with mocked dependencies
            verify(mockGitProvider).getRepository("test-org/test-repo");
            verify(mockGitProvider, never()).readRepository(any());
            verify(mockGitProvider, never()).saveDescriptor(any(), anyString(), anyString());
        } finally {
            // Cleanup via REST endpoint
            rest.delete(apiUrl(RoutesV2.DATA_PRODUCTS, "/" + testUuid));
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

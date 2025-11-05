package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderModelResourceType;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.rest.v2.mocks.GitProviderFactoryMock;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GitProviderControllerIT extends RegistryApplicationIT {

    private static final String TEST_PAT_TOKEN = "test-pat-token";
    private static final String TEST_PAT_USERNAME = "test-user";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    @AfterEach
    void tearDown() {
        // Reset the test factory mock
        gitProviderFactoryMock.reset();
    }

    @Test
    public void whenGetOrganizationsWithValidProviderThenReturnOrganizations() throws Exception {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Organization mockOrg1 = createMockOrganization("123", "test-org-1");
        Organization mockOrg2 = createMockOrganization("456", "test-org-2");
        List<Organization> mockOrganizations = Arrays.asList(mockOrg1, mockOrg2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Organization> mockPage = new PageImpl<>(mockOrganizations, pageable, 2);

        // Configure the mock GitProvider to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.listOrganizations(any(Pageable.class))).thenReturn(mockPage);

        // Create expected response objects
        OrganizationRes expectedOrg1 = new OrganizationRes("123", "test-org-1", "https://github.com/test-org-1");
        OrganizationRes expectedOrg2 = new OrganizationRes("456", "test-org-2", "https://github.com/test-org-2");

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify response structure
        JsonNode responseBody = response.getBody();
        assertThat(responseBody.has("content")).isTrue();
        assertThat(responseBody.has("totalElements")).isTrue();
        assertThat(responseBody.get("totalElements").asInt()).isEqualTo(2);

        // Verify content array
        JsonNode content = responseBody.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(2);

        // Parse and verify first organization
        OrganizationRes actualOrg1 = objectMapper.treeToValue(content.get(0), OrganizationRes.class);
        assertThat(actualOrg1).usingRecursiveComparison().isEqualTo(expectedOrg1);

        // Parse and verify second organization
        OrganizationRes actualOrg2 = objectMapper.treeToValue(content.get(1), OrganizationRes.class);
        assertThat(actualOrg2).usingRecursiveComparison().isEqualTo(expectedOrg2);
    }

    @Test
    public void whenGetOrganizationsWithInvalidProviderTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=INVALID&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetOrganizationsWithoutProviderTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - missing required providerType parameter
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then - validation should catch missing providerType at controller level
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetOrganizationsWithoutAuthenticationThenReturnUnauthorized() {
        // Given - no headers (no authentication)

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetRepositoriesWithValidParametersThenReturnRepositories() throws Exception {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Repository mockRepo1 = createMockRepository("repo1", "Test Repository 1");
        Repository mockRepo2 = createMockRepository("repo2", "Test Repository 2");
        List<Repository> mockRepositories = Arrays.asList(mockRepo1, mockRepo2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Repository> mockPage = new PageImpl<>(mockRepositories, pageable, 2);

        // Mock the GitProvider method to return our test data - use any() for all parameters
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        User mockUser = new User();
        mockUser.setId("123");
        mockUser.setUsername("testuser");
        when(mockGitProvider.getCurrentUser()).thenReturn(mockUser);
        when(mockGitProvider.listRepositories(any(), any(), any(), any())).thenReturn(mockPage);

        // Create expected response objects
        RepositoryRes expectedRepo1 = new RepositoryRes("123456", "repo1", "Test Repository 1",
                "https://github.com/test/repo1.git", "git@github.com:test/repo1.git", "main",
                null, null, null);
        RepositoryRes expectedRepo2 = new RepositoryRes("123456", "repo2", "Test Repository 2",
                "https://github.com/test/repo2.git", "git@github.com:test/repo2.git", "main",
                null, null, null);

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&showUserRepositories=true&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify response structure
        JsonNode responseBody = response.getBody();
        assertThat(responseBody.has("content")).isTrue();
        assertThat(responseBody.has("totalElements")).isTrue();
        assertThat(responseBody.get("totalElements").asInt()).isEqualTo(2);

        // Verify content array
        JsonNode content = responseBody.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(2);

        // Parse and verify first repository
        RepositoryRes actualRepo1 = objectMapper.treeToValue(content.get(0), RepositoryRes.class);
        assertThat(actualRepo1).usingRecursiveComparison().isEqualTo(expectedRepo1);

        // Parse and verify second repository
        RepositoryRes actualRepo2 = objectMapper.treeToValue(content.get(1), RepositoryRes.class);
        assertThat(actualRepo2).usingRecursiveComparison().isEqualTo(expectedRepo2);
    }

    @Test
    public void whenGetRepositoriesWithOrganizationParametersThenReturnRepositories() throws Exception {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Repository mockRepo1 = createMockRepository("org-repo-1", "Organization repository 1");
        Repository mockRepo2 = createMockRepository("org-repo-2", "Organization repository 2");
        List<Repository> mockRepositories = Arrays.asList(mockRepo1, mockRepo2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Repository> mockPage = new PageImpl<>(mockRepositories, pageable, 2);

        // Mock the GitProvider method to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.listRepositories(any(), any(), any(), any())).thenReturn(mockPage);

        // Create expected response objects
        RepositoryRes expectedRepo1 = new RepositoryRes("123456", "org-repo-1", "Organization repository 1",
                "https://github.com/test/org-repo-1.git", "git@github.com:test/org-repo-1.git", "main",
                null, null, null);
        RepositoryRes expectedRepo2 = new RepositoryRes("123456", "org-repo-2", "Organization repository 2",
                "https://github.com/test/org-repo-2.git", "git@github.com:test/org-repo-2.git", "main",
                null, null, null);

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser&organizationId=456&organizationName=testorg&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify response structure
        JsonNode responseBody = response.getBody();
        assertThat(responseBody.has("content")).isTrue();
        assertThat(responseBody.has("totalElements")).isTrue();
        assertThat(responseBody.get("totalElements").asInt()).isEqualTo(2);

        // Verify content array
        JsonNode content = responseBody.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(2);

        // Parse and verify first repository
        RepositoryRes actualRepo1 = objectMapper.treeToValue(content.get(0), RepositoryRes.class);
        assertThat(actualRepo1).usingRecursiveComparison().isEqualTo(expectedRepo1);

        // Parse and verify second repository
        RepositoryRes actualRepo2 = objectMapper.treeToValue(content.get(1), RepositoryRes.class);
        assertThat(actualRepo2).usingRecursiveComparison().isEqualTo(expectedRepo2);
    }

    @Test
    public void whenGetRepositoriesWithoutRequiredParametersThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - missing organization ID and getFromCurrentUser is false
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then - validation should catch missing userId and username at controller level
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetRepositoriesWithPaginationThenReturnPaginatedResults() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Repository mockRepo1 = createMockRepository("repo1", "Test Repository 1");
        Repository mockRepo2 = createMockRepository("repo2", "Test Repository 2");
        List<Repository> mockRepositories = Arrays.asList(mockRepo1, mockRepo2);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Repository> mockPage = new PageImpl<>(mockRepositories, pageable, 2);

        // Mock the GitProvider method to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.listRepositories(any(), any(), any(), any())).thenReturn(mockPage);
        when(mockGitProvider.getCurrentUser()).thenReturn(new User("123", "testuser", null, null, null));

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&showUserRepositories=true&page=0&size=5"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Verify pagination structure is present in response
        assertThat(response.getBody()).contains("pageable");
        assertThat(response.getBody()).contains("totalElements");
    }

    @Test
    public void whenGetOrganizationsWithPaginationThenReturnPaginatedResults() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Organization mockOrg1 = createMockOrganization("123", "org-1");
        Organization mockOrg2 = createMockOrganization("456", "org-2");
        List<Organization> mockOrganizations = Arrays.asList(mockOrg1, mockOrg2);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Organization> mockPage = new PageImpl<>(mockOrganizations, pageable, 2);

        // Mock the GitProvider method to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.listOrganizations(any(Pageable.class))).thenReturn(mockPage);

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=5"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Verify pagination structure is present in response
        assertThat(response.getBody()).contains("pageable");
        assertThat(response.getBody()).contains("totalElements");
    }

    @Test
    public void whenGetOrganizationsWithSortingThenReturnSortedResults() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Organization mockOrg1 = createMockOrganization("123", "sorted-org-1");
        Organization mockOrg2 = createMockOrganization("456", "sorted-org-2");
        List<Organization> mockOrganizations = Arrays.asList(mockOrg1, mockOrg2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Organization> mockPage = new PageImpl<>(mockOrganizations, pageable, 2);

        // Mock the GitProvider method to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.listOrganizations(any(Pageable.class))).thenReturn(mockPage);

        // When - sort by name ascending
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=10&sort=name,asc"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void whenGetRepositoriesWithSortingThenReturnSortedResults() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Repository mockRepo1 = createMockRepository("sorted-repo-1", "Sorted Repository 1");
        Repository mockRepo2 = createMockRepository("sorted-repo-2", "Sorted Repository 2");
        List<Repository> mockRepositories = Arrays.asList(mockRepo1, mockRepo2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Repository> mockPage = new PageImpl<>(mockRepositories, pageable, 2);

        // Mock the GitProvider method to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        User mockUser = new User();
        mockUser.setId("123");
        mockUser.setUsername("testuser");
        when(mockGitProvider.getCurrentUser()).thenReturn(mockUser);
        when(mockGitProvider.listRepositories(any(), any(), any(), any())).thenReturn(mockPage);

        // When - sort by name ascending
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&showUserRepositories=true&page=0&size=10&sort=name,asc"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void whenCreateRepositoryWithValidParametersThenReturnRepository() throws Exception {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Repository mockCreatedRepo = createMockRepository("test-repo", "Test repository");
        RepositoryRes expectedRepoRes = new RepositoryRes("123456", "test-repo", "Test repository",
                "https://github.com/test/test-repo.git", "git@github.com:test/test-repo.git", "main",
                null, null, null);

        // Configure the mock GitProvider to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.createRepository(any(Repository.class))).thenReturn(mockCreatedRepo);
        when(mockGitProvider.getCurrentUser()).thenReturn(new User("123", "testuser", null, null, null));

        // Create request body
        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("test-repo");
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(false);

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createRepositoryReq, headers),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        // Parse and verify response
        RepositoryRes actualRepoRes = objectMapper.treeToValue(response.getBody(), RepositoryRes.class);
        assertThat(actualRepoRes).usingRecursiveComparison().isEqualTo(expectedRepoRes);
    }

    @Test
    public void whenCreateRepositoryForOrganizationThenReturnRepository() throws Exception {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        Repository mockCreatedRepo = createMockRepository("org-repo", "Organization repository");
        RepositoryRes expectedRepoRes = new RepositoryRes("123456", "org-repo", "Organization repository",
                "https://github.com/test/org-repo.git", "git@github.com:test/org-repo.git", "main",
                null, null, null);

        // Configure the mock GitProvider to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.createRepository(any(Repository.class))).thenReturn(mockCreatedRepo);

        // Create request body
        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("org-repo");
        createRepositoryReq.setDescription("Organization repository");
        createRepositoryReq.setIsPrivate(true);

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser&organizationId=456&organizationName=testorg"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createRepositoryReq, headers),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        // Parse and verify response
        RepositoryRes actualRepoRes = objectMapper.treeToValue(response.getBody(), RepositoryRes.class);
        assertThat(actualRepoRes).usingRecursiveComparison().isEqualTo(expectedRepoRes);
    }

    @Test
    public void whenCreateRepositoryWithoutRequiredParametersThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Create request body
        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(false);

        // When - missing required userId and username parameters
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createRepositoryReq, headers),
                String.class
        );

        // Then - validation should catch missing userId and username at controller level
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenCreateRepositoryWithoutAuthenticationThenReturnBadRequest() {
        // Given - no headers (no authentication)
        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("test-repo");
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(false);

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createRepositoryReq, new HttpHeaders()),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenCreateRepositoryWithInvalidProviderTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Create request body
        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("test-repo");
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(false);

        // When - invalid provider type
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=INVALID&userId=123&username=testuser"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createRepositoryReq, headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenCreateRepositoryWithEmptyRequestBodyThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - empty request body
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser"),
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(new CreateRepositoryReqRes(), headers),
                String.class
        );

        // Then - validation should catch missing required fields in request body
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourceDefinitionsWithValidProviderAndResourceThenReturnDefinitions() throws Exception {
        // Given
        ProviderCustomResourceDefinition mockDefinition1 = new ProviderCustomResourceDefinition("project", "OBJECT", true);
        List<ProviderCustomResourceDefinition> mockDefinitions = Arrays.asList(mockDefinition1);

        // Configure the mock GitProvider to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.getProviderCustomResourceDefinitions(any(GitProviderModelResourceType.class)))
                .thenReturn(mockDefinitions);

        // Create expected response object
        ProviderCustomResourceDefinitionRes expectedDefinition = new ProviderCustomResourceDefinitionRes("project", "OBJECT", true);

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources/definitions?resourceName=repository&providerType=BITBUCKET"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify response structure
        JsonNode responseBody = response.getBody();
        assertThat(responseBody.has("definitions")).isTrue();

        // Verify definitions array
        JsonNode definitions = responseBody.get("definitions");
        assertThat(definitions.isArray()).isTrue();
        assertThat(definitions.size()).isEqualTo(1);

        // Parse and verify definition
        ProviderCustomResourceDefinitionRes actualDefinition = objectMapper.treeToValue(definitions.get(0), ProviderCustomResourceDefinitionRes.class);
        assertThat(actualDefinition).usingRecursiveComparison().isEqualTo(expectedDefinition);
    }

    @Test
    public void whenGetCustomResourceDefinitionsWithProviderReturningEmptyListThenReturnEmptyList() {
        // Given
        // Configure the mock GitProvider to return empty list (e.g., GitHub doesn't have custom definitions for repository)
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.getProviderCustomResourceDefinitions(any(GitProviderModelResourceType.class)))
                .thenReturn(Arrays.asList());

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources/definitions?resourceName=repository&providerType=GITHUB"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify response structure
        JsonNode responseBody = response.getBody();
        assertThat(responseBody.has("definitions")).isTrue();

        // Verify definitions array is empty
        JsonNode definitions = responseBody.get("definitions");
        assertThat(definitions.isArray()).isTrue();
        assertThat(definitions.size()).isEqualTo(0);
    }

    @Test
    public void whenGetCustomResourceDefinitionsWithInvalidResourceTypeThenReturnBadRequest() {
        // Given - invalid resource type

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources/definitions?resourceName=INVALID&providerType=BITBUCKET"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourceDefinitionsWithoutResourceNameThenReturnBadRequest() {
        // Given - missing resourceName parameter

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources/definitions?providerType=BITBUCKET"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourceDefinitionsWithoutProviderTypeThenReturnBadRequest() {
        // Given - missing providerType parameter

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources/definitions?resourceName=repository"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourceDefinitionsWithInvalidProviderTypeThenReturnBadRequest() {
        // Given - invalid provider type

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources/definitions?resourceName=repository&providerType=INVALID"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates test headers with PAT authentication
     */
    private HttpHeaders createTestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", TEST_PAT_TOKEN);
        headers.set("x-odm-gpauth-param-username", TEST_PAT_USERNAME);
        return headers;
    }

    // Helper methods to create mock objects
    private Organization createMockOrganization(String id, String name) {
        Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setUrl("https://github.com/" + name);
        return org;
    }

    private Repository createMockRepository(String name, String description) {
        Repository repo = new Repository();
        repo.setId("123456");
        repo.setName(name);
        repo.setDescription(description);
        repo.setCloneUrlHttp("https://github.com/test/" + name + ".git");
        repo.setCloneUrlSsh("git@github.com:test/" + name + ".git");
        repo.setDefaultBranch("main");
        return repo;
    }

    @Test
    public void whenGetCustomResourcesWithValidProviderAndResourceTypeThenReturnResources() throws Exception {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Setup mock data
        ProviderCustomResource mockResource1 = createMockCustomResource("project-1", "Project 1");
        ProviderCustomResource mockResource2 = createMockCustomResource("project-2", "Project 2");
        List<ProviderCustomResource> mockResources = Arrays.asList(mockResource1, mockResource2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProviderCustomResource> mockPage = new PageImpl<>(mockResources, pageable, 2);

        // Configure the mock GitProvider to return our test data
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.getProviderCustomResources(any(String.class), any(), any(Pageable.class)))
                .thenReturn(mockPage);

        // Create expected response objects
        ProviderCustomResourceRes expectedResource1 = new ProviderCustomResourceRes("project-1", "Project 1", null);
        ProviderCustomResourceRes expectedResource2 = new ProviderCustomResourceRes("project-2", "Project 2", null);

        // When
        ResponseEntity<JsonNode> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources?resourceType=project&providerType=BITBUCKET&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                JsonNode.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Verify response structure
        JsonNode responseBody = response.getBody();
        assertThat(responseBody.has("content")).isTrue();
        assertThat(responseBody.has("totalElements")).isTrue();
        assertThat(responseBody.get("totalElements").asInt()).isEqualTo(2);

        // Verify content array
        JsonNode content = responseBody.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(2);

        // Parse and verify first resource
        ProviderCustomResourceRes actualResource1 = objectMapper.treeToValue(content.get(0), ProviderCustomResourceRes.class);
        assertThat(actualResource1.getIdentifier()).isEqualTo(expectedResource1.getIdentifier());
        assertThat(actualResource1.getDisplayName()).isEqualTo(expectedResource1.getDisplayName());

        // Parse and verify second resource
        ProviderCustomResourceRes actualResource2 = objectMapper.treeToValue(content.get(1), ProviderCustomResourceRes.class);
        assertThat(actualResource2.getIdentifier()).isEqualTo(expectedResource2.getIdentifier());
        assertThat(actualResource2.getDisplayName()).isEqualTo(expectedResource2.getDisplayName());
    }

    @Test
    public void whenGetCustomResourcesWithoutResourceTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - missing required resourceType parameter
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources?providerType=BITBUCKET&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourcesWithoutProviderTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - missing required providerType parameter
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources?resourceType=project&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourcesWithoutAuthenticationThenReturnBadRequest() {
        // Given - no headers (no authentication)

        // When
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources?resourceType=project&providerType=BITBUCKET&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(new HttpHeaders()),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourcesWithInvalidProviderTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - invalid provider type
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources?resourceType=project&providerType=INVALID&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetCustomResourcesWithUnsupportedResourceTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // Configure the mock GitProvider to throw BadRequestException for unsupported resource type
        GitProvider mockGitProvider = gitProviderFactoryMock.getMockGitProvider();
        when(mockGitProvider.getProviderCustomResources(any(String.class), any(), any(Pageable.class)))
                .thenThrow(new BadRequestException("Unsupported retrieval for resource type: unsupported"));

        // When - unsupported resource type
        ResponseEntity<String> response = rest.exchange(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/custom-resources?resourceType=unsupported&providerType=BITBUCKET&page=0&size=10"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Helper method to create mock custom resource
    private ProviderCustomResource createMockCustomResource(String identifier, String displayName) {
        ProviderCustomResource resource = new ProviderCustomResource();
        resource.setIdentifier(identifier);
        resource.setDisplayName(displayName);
        resource.setContent(null); // Can be set to a JsonNode if needed
        return resource;
    }

}

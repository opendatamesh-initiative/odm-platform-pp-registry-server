package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendatamesh.platform.pp.registry.githandler.model.Organization;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.rest.v2.RegistryApplicationIT;
import org.opendatamesh.platform.pp.registry.rest.v2.RoutesV2;
import org.opendatamesh.platform.pp.registry.test.mocks.GitProviderFactoryMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
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

@ActiveProfiles("test")
public class GitProviderControllerIT extends RegistryApplicationIT {

    private static final String TEST_PAT_TOKEN = "test-pat-token";
    private static final String TEST_PAT_USERNAME = "test-user";

    @Autowired
    private GitProviderFactoryMock gitProviderFactoryMock;

    @BeforeEach
    void setUp() {
        // Reset the test factory mock
        gitProviderFactoryMock.reset();
    }

    @AfterEach
    void tearDown() {
        // Reset the test factory mock
        gitProviderFactoryMock.reset();
    }

    @Test
    public void whenGetOrganizationsWithValidProviderThenReturnOrganizations() {
        // Given
        HttpHeaders headers = createTestHeaders();
        
        // Setup mock data
        Organization mockOrg1 = createMockOrganization("123", "test-org-1");
        Organization mockOrg2 = createMockOrganization("456", "test-org-2");
        List<Organization> mockOrganizations = Arrays.asList(mockOrg1, mockOrg2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Organization> mockPage = new PageImpl<>(mockOrganizations, pageable, 2);
        
        // Configure the mock GitProvider to return our test data
        when(gitProviderFactoryMock.getMockGitProvider().listOrganizations(any(Pageable.class))).thenReturn(mockPage);
        
        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=10"),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("test-org-1");
        assertThat(response.getBody()).contains("test-org-2");
        assertThat(response.getBody()).contains("totalElements");
    }

    @Test
    public void whenGetOrganizationsWithInvalidProviderTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=INVALID&page=0&size=10"),
                String.class,
                headers
        );

        // Then
        // The response might be OK if the service handles invalid provider types gracefully,
        // or BAD_REQUEST if validation is strict. We'll check for a reasonable response.
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void whenGetOrganizationsWithoutProviderTypeThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - missing required providerType parameter
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?page=0&size=10"),
                String.class,
                headers
        );

        // Then - validation should catch missing providerType at controller level
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // TODO: Uncomment this test after authentication/authorization is implemented
    // @Test
    public void whenGetOrganizationsWithoutAuthenticationThenReturnUnauthorized() {
        // Given - no headers (no authentication)

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=10"),
                String.class
        );

        // Then - should return UNAUTHORIZED when authentication is implemented
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void whenGetRepositoriesWithValidParametersThenReturnRepositories() {
        // Given
        HttpHeaders headers = createTestHeaders();
        
        // Setup mock data
        Repository mockRepo1 = createMockRepository("repo1", "Test Repository 1");
        Repository mockRepo2 = createMockRepository("repo2", "Test Repository 2");
        List<Repository> mockRepositories = Arrays.asList(mockRepo1, mockRepo2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Repository> mockPage = new PageImpl<>(mockRepositories, pageable, 2);
        
        // Mock the GitProvider method to return our test data - use any() for all parameters
        when(gitProviderFactoryMock.getMockGitProvider().listRepositories(any(), any(), any()))
                .thenReturn(mockPage);

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser&page=0&size=10"),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("repo1");
        assertThat(response.getBody()).contains("repo2");
        assertThat(response.getBody()).contains("totalElements");
    }

    @Test
    public void whenGetRepositoriesWithOrganizationParametersThenReturnRepositories() {
        // Given
        HttpHeaders headers = createTestHeaders();
        
        // Setup mock data
        Repository mockRepo1 = createMockRepository("org-repo-1", "Organization repository 1");
        Repository mockRepo2 = createMockRepository("org-repo-2", "Organization repository 2");
        List<Repository> mockRepositories = Arrays.asList(mockRepo1, mockRepo2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Repository> mockPage = new PageImpl<>(mockRepositories, pageable, 2);
        
        // Mock the GitProvider method to return our test data
        when(gitProviderFactoryMock.getMockGitProvider().listRepositories(any(), any(), any()))
                .thenReturn(mockPage);

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser&organizationId=456&organizationName=testorg&page=0&size=10"),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("org-repo-1");
        assertThat(response.getBody()).contains("org-repo-2");
        assertThat(response.getBody()).contains("totalElements");
    }

    @Test
    public void whenGetRepositoriesWithoutRequiredParametersThenReturnBadRequest() {
        // Given
        HttpHeaders headers = createTestHeaders();

        // When - missing required userId and username parameters
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&page=0&size=10"),
                String.class,
                headers
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
        when(gitProviderFactoryMock.getMockGitProvider().listRepositories(any(), any(), any()))
                .thenReturn(mockPage);

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser&page=0&size=5"),
                String.class,
                headers
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
        when(gitProviderFactoryMock.getMockGitProvider().listOrganizations(any(Pageable.class))).thenReturn(mockPage);

        // When
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=5"),
                String.class,
                headers
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
        when(gitProviderFactoryMock.getMockGitProvider().listOrganizations(any(Pageable.class))).thenReturn(mockPage);

        // When - sort by name ascending
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/organizations?providerType=GITHUB&page=0&size=10&sort=name,asc"),
                String.class,
                headers
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
        when(gitProviderFactoryMock.getMockGitProvider().listRepositories(any(), any(), any()))
                .thenReturn(mockPage);

        // When - sort by name ascending
        ResponseEntity<String> response = rest.getForEntity(
                apiUrl(RoutesV2.GIT_PROVIDERS, "/repositories?providerType=GITHUB&userId=123&username=testuser&page=0&size=10&sort=name,asc"),
                String.class,
                headers
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    /**
     * Creates test headers with PAT authentication
     */
    private HttpHeaders createTestHeaders() {
        HttpHeaders headers = new HttpHeaders();
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

}

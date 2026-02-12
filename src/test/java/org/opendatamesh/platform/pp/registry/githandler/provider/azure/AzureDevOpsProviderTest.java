package org.opendatamesh.platform.pp.registry.githandler.provider.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.credentials.AzurePatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.model.filters.ListCommitFilters;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.checkconnection.AzureCheckConnectionUserResponseRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getcurrentuser.AzureGetCurrentUserUserResponseRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getrepository.AzureGetRepositoryProjectListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getrepository.AzureGetRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listbranches.AzureListBranchesBranchListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits.AzureListCommitsCommitListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories.AzureListRepositoriesProjectListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories.AzureListRepositoriesRepositoryListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listtags.AzureListTagsTagListRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureDevOpsProviderTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private AzureDevOpsProvider azureDevOpsProvider;
    private GitProviderCredential credential;
    private String baseUrl = "https://dev.azure.com";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        credential = new AzurePatCredential("test-token");
        azureDevOpsProvider = new AzureDevOpsProvider(baseUrl, restTemplate, credential);
    }

    @Test
    void whenBaseUrlIsNullThenThrowException() {
        assertThatThrownBy(() -> new AzureDevOpsProvider(null, restTemplate, credential))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenBaseUrlIsBlankThenThrowException() {
        assertThatThrownBy(() -> new AzureDevOpsProvider("  ", restTemplate, credential))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenCheckConnectionCalledThenAssertConnectionSuccessful() throws Exception {
        // Load JSON response
        AzureCheckConnectionUserResponseRes userRes = loadJson("azure/check_connection.json", AzureCheckConnectionUserResponseRes.class);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/_apis/connectionData"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureCheckConnectionUserResponseRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        azureDevOpsProvider.checkConnection();

        // Verify
        verify(restTemplate, times(1)).exchange(
                contains("/_apis/connectionData"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureCheckConnectionUserResponseRes.class)
        );
    }

    @Test
    void whenGetCurrentUserCalledThenAssertUserReturned() throws Exception {
        // Load JSON response
        AzureGetCurrentUserUserResponseRes userRes = loadJson("azure/get_current_user.json", AzureGetCurrentUserUserResponseRes.class);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/_apis/connectionData"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetCurrentUserUserResponseRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        User user = azureDevOpsProvider.getCurrentUser();

        // Verify
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/_apis/connectionData"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetCurrentUserUserResponseRes.class)
        );
    }

    @Test
    void whenListOrganizationsCalledThenAssertOrganizationsReturned() {
        Pageable pageable = PageRequest.of(0, 20);

        // Test
        Page<Organization> organizations = azureDevOpsProvider.listOrganizations(pageable);

        // Verify
        assertThat(organizations).isNotNull();
        assertThat(organizations.getContent()).isNotEmpty();
    }

    @Test
    void whenGetOrganizationCalledThenAssertOrganizationReturned() {
        String orgId = "default-org";

        // Test
        Optional<Organization> organization = azureDevOpsProvider.getOrganization(orgId);

        // Verify
        assertThat(organization).isPresent();
        assertThat(organization.get().getName()).isEqualTo(orgId);
    }

    @Test
    void whenListMembersCalledThenAssertMembersReturned() throws Exception {
        Organization org = new Organization("default-org", "default-org", baseUrl);
        Pageable pageable = PageRequest.of(0, 20);

        // Mock getCurrentUser call
        AzureGetCurrentUserUserResponseRes userRes = loadJson("azure/get_current_user.json", AzureGetCurrentUserUserResponseRes.class);
        when(restTemplate.exchange(
                contains("/_apis/connectionData"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetCurrentUserUserResponseRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        Page<User> members = azureDevOpsProvider.listMembers(org, pageable);

        // Verify
        assertThat(members).isNotNull();
        assertThat(members.getContent()).isNotEmpty();
    }

    @Test
    void whenListRepositoriesCalledThenAssertRepositoriesReturned() throws Exception {
        // Load JSON responses
        AzureListRepositoriesProjectListRes projectsRes = loadJson("azure/list_projects.json", AzureListRepositoriesProjectListRes.class);
        AzureListRepositoriesRepositoryListRes reposRes = loadJson("azure/list_repositories.json", AzureListRepositoriesRepositoryListRes.class);
        Pageable pageable = PageRequest.of(0, 20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        // Mock RestTemplate responses
        when(restTemplate.exchange(
                eq(baseUrl + "/_apis/projects?api-version=7.1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListRepositoriesProjectListRes.class)
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        when(restTemplate.exchange(
                eq(baseUrl + "/{projectName}/_apis/git/repositories?api-version={apiVersion}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListRepositoriesRepositoryListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(reposRes, HttpStatus.OK));

        // Test
        Page<Repository> repositories = azureDevOpsProvider.listRepositories(null, null, parameters, pageable);

        // Verify
        assertThat(repositories).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                eq(baseUrl + "/_apis/projects?api-version=7.1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListRepositoriesProjectListRes.class)
        );
        verify(restTemplate, atLeastOnce()).exchange(
                eq(baseUrl + "/{projectName}/_apis/git/repositories?api-version={apiVersion}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListRepositoriesRepositoryListRes.class),
                anyMap()
        );
    }

    @Test
    void whenGetRepositoryCalledThenAssertRepositoryReturned() throws Exception {
        // Load JSON responses
        AzureGetRepositoryProjectListRes projectsRes = loadJson("azure/list_projects.json", AzureGetRepositoryProjectListRes.class);
        AzureGetRepositoryRepositoryRes repoRes = loadJson("azure/get_repository.json", AzureGetRepositoryRepositoryRes.class);
        String repoId = "test-repo-id";

        // Mock RestTemplate responses
        when(restTemplate.exchange(
                eq(baseUrl + "/_apis/projects?api-version=7.1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryProjectListRes.class)
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        when(restTemplate.exchange(
                eq(baseUrl + "/{projectName}/_apis/git/repositories/{repoId}?api-version={apiVersion}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryRepositoryRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(repoRes, HttpStatus.OK));

        // Test
        Optional<Repository> repository = azureDevOpsProvider.getRepository(repoId, null);

        // Verify
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                eq(baseUrl + "/_apis/projects?api-version=7.1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryProjectListRes.class)
        );
        verify(restTemplate, atLeastOnce()).exchange(
                eq(baseUrl + "/{projectName}/_apis/git/repositories/{repoId}?api-version={apiVersion}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryRepositoryRes.class),
                anyMap()
        );
    }

    @Test
    void whenListCommitsCalledThenAssertCommitsReturned() throws Exception {
        // Load JSON response
        AzureListCommitsCommitListRes commitsRes = loadJson("azure/list_commits.json", AzureListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = azureDevOpsProvider.listCommits(repository, null, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                anyMap()
        );
    }

    @Test
    void whenListBranchesCalledThenAssertBranchesReturned() throws Exception {
        // Load JSON response
        AzureListBranchesBranchListRes branchesRes = loadJson("azure/list_branches.json", AzureListBranchesBranchListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/refs?api-version={apiVersion}&filter={filter}&$top={top}&$skip={skip}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListBranchesBranchListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = azureDevOpsProvider.listBranches(repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/refs?api-version={apiVersion}&filter={filter}&$top={top}&$skip={skip}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListBranchesBranchListRes.class),
                anyMap()
        );
    }

    @Test
    void whenListTagsCalledThenAssertTagsReturned() throws Exception {
        // Load JSON response
        AzureListTagsTagListRes tagsRes = loadJson("azure/list_tags.json", AzureListTagsTagListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/refs?api-version={apiVersion}&filter={filter}&$top={top}&$skip={skip}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListTagsTagListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = azureDevOpsProvider.listTags(repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/refs?api-version={apiVersion}&filter={filter}&$top={top}&$skip={skip}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListTagsTagListRes.class),
                anyMap()
        );
    }

    @Test
    void whenListCommitsCalledWithTagFiltersThenAssertCommitsReturned() throws Exception {
        // Given
        AzureListCommitsCommitListRes commitsRes = loadJson("azure/list_commits_filtered.json", AzureListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters("v1.0.0", "v2.0.0", null, null, null, null);
        
        // Mock RestTemplate response for batch commits
        when(restTemplate.exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}&itemVersion.version={from}&itemVersion.versionType={fromType}&compareVersion.version={to}&compareVersion.versionType={toType}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // When
        Page<Commit> commits = azureDevOpsProvider.listCommits(repository, filters, pageable);

        List<Commit> expectedCommits = new ArrayList<>();
        expectedCommits.add(new Commit("aaa1bbb2ccc3ddd4eee5fff61111222233334444", "Updated README.md", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:56Z"))));
        expectedCommits.add(new Commit("bbb2ccc3ddd4eee5fff611112222333344445555", "Updated README.md 3", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:49Z"))));
        expectedCommits.add(new Commit("ccc3ddd4eee5fff6111122223333444455556666", "Updated README.md 2", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:37Z"))));

        // Then
        // Verify
        assertThat(commits).isNotNull();
        assertThat(commits.getContent()).isNotEmpty();
        assertThat(commits.getContent().size()).isEqualTo(commits.getContent().size());
        assertThat(commits.getContent())
                .usingRecursiveComparison()
                .isEqualTo(expectedCommits);

        Map<String, Object> queryParams = Map.of(
                "projectId", "default-project",
                "repoId", "test-repo-id",
                "apiVersion", "7.1",
                "from", "v1.0.0",
                "fromType", "tag",
                "to", "v2.0.0",
                "toType", "tag",
                "top", 20,
                "skip", 0
        );

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}&itemVersion.version={from}&itemVersion.versionType={fromType}&compareVersion.version={to}&compareVersion.versionType={toType}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                eq(queryParams)
        );
    }

    @Test
    void whenListCommitsCalledWithOnlyFromTagFilterThenAssertCommitsReturned() throws Exception {
        // Given
        AzureListCommitsCommitListRes commitsRes = loadJson("azure/list_commits_filtered.json", AzureListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters("v1.0.0", null, null, null, null, null);

        // Mock RestTemplate response for batch commits
        when(restTemplate.exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}&itemVersion.version={from}&itemVersion.versionType={fromType}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // When
        Page<Commit> commits = azureDevOpsProvider.listCommits(repository, filters, pageable);

        List<Commit> expectedCommits = new ArrayList<>();
        expectedCommits.add(new Commit("aaa1bbb2ccc3ddd4eee5fff61111222233334444", "Updated README.md", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:56Z"))));
        expectedCommits.add(new Commit("bbb2ccc3ddd4eee5fff611112222333344445555", "Updated README.md 3", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:49Z"))));
        expectedCommits.add(new Commit("ccc3ddd4eee5fff6111122223333444455556666", "Updated README.md 2", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:37Z"))));

        // Then
        // Verify
        assertThat(commits).isNotNull();
        assertThat(commits.getContent()).isNotEmpty();
        assertThat(commits.getContent().size()).isEqualTo(commits.getContent().size());
        assertThat(commits.getContent())
                .usingRecursiveComparison()
                .isEqualTo(expectedCommits);

        Map<String, Object> queryParams = Map.of(
                "projectId", "default-project",
                "repoId", "test-repo-id",
                "apiVersion", "7.1",
                "from", "v1.0.0",
                "fromType", "tag",
                "top", 20,
                "skip", 0
        );

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}&itemVersion.version={from}&itemVersion.versionType={fromType}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                eq(queryParams)
        );
    }

    @Test
    void whenListCommitsCalledWithOnlyToTagFilterThenAssertCommitsReturned() throws Exception {
        // Given
        AzureListCommitsCommitListRes commitsRes = loadJson("azure/list_commits_filtered.json", AzureListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters(null, "v1.0.0", null, null, null, null);

        // Mock RestTemplate response for batch commits
        when(restTemplate.exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}&compareVersion.version={to}&compareVersion.versionType={toType}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // When
        Page<Commit> commits = azureDevOpsProvider.listCommits(repository, filters, pageable);

        List<Commit> expectedCommits = new ArrayList<>();
        expectedCommits.add(new Commit("aaa1bbb2ccc3ddd4eee5fff61111222233334444", "Updated README.md", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:56Z"))));
        expectedCommits.add(new Commit("bbb2ccc3ddd4eee5fff611112222333344445555", "Updated README.md 3", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:49Z"))));
        expectedCommits.add(new Commit("ccc3ddd4eee5fff6111122223333444455556666", "Updated README.md 2", "eloria.starweaver@mythicforge.realm", Date.from(Instant.parse("2025-12-03T15:42:37Z"))));

        // Then
        // Verify
        assertThat(commits).isNotNull();
        assertThat(commits.getContent()).isNotEmpty();
        assertThat(commits.getContent().size()).isEqualTo(commits.getContent().size());
        assertThat(commits.getContent())
                .usingRecursiveComparison()
                .isEqualTo(expectedCommits);

        Map<String, Object> queryParams = Map.of(
                "projectId", "default-project",
                "repoId", "test-repo-id",
                "apiVersion", "7.1",
                "to", "v1.0.0",
                "toType", "tag",
                "top", 20,
                "skip", 0
        );

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/commits?api-version={apiVersion}&$top={top}&$skip={skip}&compareVersion.version={to}&compareVersion.versionType={toType}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class),
                eq(queryParams)
        );
    }

    @Test
    void whenListCommitsCalledWithFromTagNameFilterEmptyThenThrowsBadRequestException() throws Exception {
        // Given
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters("", "v2.0.0", null, null, null, null);

        // When & Then
        assertThatThrownBy(() -> azureDevOpsProvider.listCommits(
                repository, filters, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("From or to parameter are empty");
    }

    @Test
    void whenListCommitsCalledWithToTagNameFilterEmptyThenThrowsBadRequestException() throws Exception {
        // Given
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        repository.setOwnerId("default-project");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters("v1.0.0", "", null, null, null, null);

        // When & Then
        assertThatThrownBy(() -> azureDevOpsProvider.listCommits(
                repository, filters, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("From or to parameter are empty");
    }

    /**
     * Helper method to load JSON from test resources and deserialize it
     */
    private <T> T loadJson(String resourcePath, Class<T> clazz) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        return objectMapper.readValue(inputStream, clazz);
    }
}


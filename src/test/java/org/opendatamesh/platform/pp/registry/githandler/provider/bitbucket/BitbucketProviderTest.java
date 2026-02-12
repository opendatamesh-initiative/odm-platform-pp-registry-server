package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.credentials.BitbucketPatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.model.filters.ListCommitFilters;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.checkconnection.BitbucketCheckConnectionUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getcurrentuser.BitbucketGetCurrentUserUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getorganization.BitbucketGetOrganizationWorkspaceRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getrepository.BitbucketGetRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches.BitbucketListBranchesBranchListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits.BitbucketListCommitsCommitListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers.BitbucketListMembersUserListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations.BitbucketListOrganizationsWorkspaceListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects.BitbucketListProjectsProjectListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories.BitbucketListRepositoriesRepositoryListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags.BitbucketListTagsTagListRes;
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
class BitbucketProviderTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private BitbucketProvider bitbucketProvider;
    private GitProviderCredential credential;
    private String baseUrl = "https://api.bitbucket.org/2.0";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        credential = new BitbucketPatCredential("test-user", "test-token");
        bitbucketProvider = new BitbucketProvider(baseUrl, restTemplate, credential);
    }

    @Test
    void whenBaseUrlIsNullThenThrowException() {
        assertThatThrownBy(() -> new BitbucketProvider(null, restTemplate, credential))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenBaseUrlIsBlankThenThrowException() {
        assertThatThrownBy(() -> new BitbucketProvider("  ", restTemplate, credential))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenCheckConnectionCalledThenAssertConnectionSuccessful() throws Exception {
        // Load JSON response
        BitbucketCheckConnectionUserRes userRes = loadJson("bitbucket/get_current_user.json", BitbucketCheckConnectionUserRes.class);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketCheckConnectionUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        bitbucketProvider.checkConnection();

        // Verify
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketCheckConnectionUserRes.class)
        );
    }

    @Test
    void whenGetCurrentUserCalledThenAssertUserReturned() throws Exception {
        // Load JSON response
        BitbucketGetCurrentUserUserRes userRes = loadJson("bitbucket/get_current_user.json", BitbucketGetCurrentUserUserRes.class);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetCurrentUserUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        User user = bitbucketProvider.getCurrentUser();

        // Verify
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetCurrentUserUserRes.class)
        );
    }

    @Test
    void whenListOrganizationsCalledThenAssertOrganizationsReturned() throws Exception {
        // Load JSON response
        BitbucketListOrganizationsWorkspaceListRes workspacesRes = loadJson("bitbucket/list_organizations.json", BitbucketListOrganizationsWorkspaceListRes.class);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/workspaces?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(workspacesRes, HttpStatus.OK));

        // Test
        Page<Organization> organizations = bitbucketProvider.listOrganizations(pageable);

        // Verify
        assertThat(organizations).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/workspaces?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class),
                anyMap()
        );
    }

    @Test
    void whenGetOrganizationCalledThenAssertOrganizationReturned() throws Exception {
        // Load JSON responses
        BitbucketListOrganizationsWorkspaceListRes workspacesRes = loadJson("bitbucket/list_organizations.json", BitbucketListOrganizationsWorkspaceListRes.class);
        BitbucketGetOrganizationWorkspaceRes workspaceRes = loadJson("bitbucket/get_organization.json", BitbucketGetOrganizationWorkspaceRes.class);
        String workspaceSlug = "test-org";
        
        // Mock listOrganizations call (called internally by getOrganization)
        when(restTemplate.exchange(
                eq(baseUrl + "/workspaces?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(workspacesRes, HttpStatus.OK));
        
        // Mock getOrganization call - it uses {identifier} as the variable name
        when(restTemplate.exchange(
                eq(baseUrl + "/workspaces/{identifier}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetOrganizationWorkspaceRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(workspaceRes, HttpStatus.OK));

        // Test
        Optional<Organization> organization = bitbucketProvider.getOrganization(workspaceSlug);

        // Verify
        assertThat(organization).isPresent();
        assertThat(organization.get().getName()).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                eq(baseUrl + "/workspaces/{identifier}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetOrganizationWorkspaceRes.class),
                anyMap()
        );
    }

    @Test
    void whenListMembersCalledThenAssertMembersReturned() throws Exception {
        // Load JSON response
        BitbucketListMembersUserListRes membersRes = loadJson("bitbucket/list_members.json", BitbucketListMembersUserListRes.class);
        Organization org = new Organization("test-org", "test-org", "https://bitbucket.org/test-org");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/workspaces/{workspaceName}/members?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListMembersUserListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(membersRes, HttpStatus.OK));

        // Test
        Page<User> members = bitbucketProvider.listMembers(org, pageable);

        // Verify
        assertThat(members).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/workspaces/{workspaceName}/members?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListMembersUserListRes.class),
                anyMap()
        );
    }

    @Test
    void whenListRepositoriesCalledThenAssertRepositoriesReturned() throws Exception {
        // Load JSON response
        BitbucketListRepositoriesRepositoryListRes reposRes = loadJson("bitbucket/list_repositories.json", BitbucketListRepositoriesRepositoryListRes.class);
        Pageable pageable = PageRequest.of(0, 20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        User user = new User("123", "test-user", "Test User", null, null);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListRepositoriesRepositoryListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(reposRes, HttpStatus.OK));

        // Test
        Page<Repository> repositories = bitbucketProvider.listRepositories(null, user, parameters, pageable);

        // Verify
        assertThat(repositories).isNotNull();
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListRepositoriesRepositoryListRes.class),
                anyMap()
        );
    }

    @Test
    void whenGetRepositoryCalledThenAssertRepositoryReturned() throws Exception {
        // Load JSON response
        BitbucketGetRepositoryRepositoryRes repoRes = loadJson("bitbucket/get_repository.json", BitbucketGetRepositoryRepositoryRes.class);
        String repoId = "test-repo";
        String ownerId = "test-user";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{ownerId}/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetRepositoryRepositoryRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(repoRes, HttpStatus.OK));

        // Test
        Optional<Repository> repository = bitbucketProvider.getRepository(repoId, ownerId);

        // Verify
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{ownerId}/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetRepositoryRepositoryRes.class),
                anyMap()
        );
    }

    @Test
    void whenListCommitsCalledThenAssertCommitsReturned() throws Exception {
        // Load JSON response
        BitbucketListCommitsCommitListRes commitsRes = loadJson("bitbucket/list_commits.json", BitbucketListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo");
        repository.setOwnerId("test-user");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = bitbucketProvider.listCommits(repository, null, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
                anyMap()
        );
    }

    @Test
    void whenListBranchesCalledThenAssertBranchesReturned() throws Exception {
        // Load JSON response
        BitbucketListBranchesBranchListRes branchesRes = loadJson("bitbucket/list_branches.json", BitbucketListBranchesBranchListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo");
        repository.setOwnerId("test-user");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/refs/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListBranchesBranchListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = bitbucketProvider.listBranches(repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/refs/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListBranchesBranchListRes.class),
                anyMap()
        );
    }

    @Test
    void whenListTagsCalledThenAssertTagsReturned() throws Exception {
        // Load JSON response
        BitbucketListTagsTagListRes tagsRes = loadJson("bitbucket/list_tags.json", BitbucketListTagsTagListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo");
        repository.setOwnerId("test-user");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/refs/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListTagsTagListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = bitbucketProvider.listTags(repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/refs/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListTagsTagListRes.class),
                anyMap()
        );
    }

    @Test
    void whenGetProviderCustomResourcesCalledWithProjectAndSingleWorkspaceThenAssertProjectsReturned() throws Exception {
        // Load JSON response
        BitbucketListProjectsProjectListRes projectsRes = loadJson("bitbucket/list_projects.json", BitbucketListProjectsProjectListRes.class);
        Pageable pageable = PageRequest.of(0, 20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("organization", "test-org");
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/workspaces/{workspaceName}/projects?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        // Test
        Page<ProviderCustomResource> customResources = bitbucketProvider.getProviderCustomResources("project", parameters, pageable);

        // Verify
        assertThat(customResources).isNotNull();
        assertThat(customResources.getContent()).isNotEmpty();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/workspaces/{workspaceName}/projects?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class),
                anyMap()
        );
    }

    @Test
    void whenGetProviderCustomResourcesCalledWithProjectAndNoWorkspaceThenAssertProjectsReturned() throws Exception {
        // Load JSON responses
        BitbucketListOrganizationsWorkspaceListRes workspacesRes = loadJson("bitbucket/list_organizations.json", BitbucketListOrganizationsWorkspaceListRes.class);
        BitbucketListProjectsProjectListRes projectsRes = loadJson("bitbucket/list_projects.json", BitbucketListProjectsProjectListRes.class);
        Pageable pageable = PageRequest.of(0, 20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        
        // Mock RestTemplate responses - first for listing workspaces, then for listing projects
        // Note: listAllProjectsForWorkspace uses pagelen=100, not the pageable size
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(workspacesRes, HttpStatus.OK));
        
        when(restTemplate.exchange(
                eq(baseUrl + "/workspaces/{workspaceName}/projects?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        // Test
        Page<ProviderCustomResource> customResources = bitbucketProvider.getProviderCustomResources("project", parameters, pageable);

        // Verify
        assertThat(customResources).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class),
                anyMap()
        );
        verify(restTemplate, atLeastOnce()).exchange(
                eq(baseUrl + "/workspaces/{workspaceName}/projects?page={page}&pagelen={pagelen}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class),
                anyMap()
        );
    }

    @Test
    void whenGetProviderCustomResourcesCalledWithUnsupportedResourceTypeThenAssertBadRequestExceptionThrown() {
        Pageable pageable = PageRequest.of(0, 20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        
        // Test - should throw BadRequestException
        assertThatThrownBy(() -> {
            bitbucketProvider.getProviderCustomResources("unsupported-type", parameters, pageable);
        }).isInstanceOf(org.opendatamesh.platform.pp.registry.exceptions.BadRequestException.class)
                .hasMessageContaining("Bitbucket Provider, unsupported retrieval for resource type: unsupported-type");
    }

    @Test
    void whenListCommitsCalledWithTagFiltersThenAssertCommitsReturned() throws Exception {
        // Given
        BitbucketListCommitsCommitListRes filteredCommitsRes = loadJson("bitbucket/list_commits_filtered.json", BitbucketListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo");
        repository.setOwnerId("test-user");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters("v1.0.0", "v2.0.0", null, null, null, null);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}&exclude={from}&include={to}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(filteredCommitsRes, HttpStatus.OK));

        // When
        Page<Commit> commits = bitbucketProvider.listCommits(repository, filters, pageable);

        List<Commit> expectedCommits = new ArrayList<>();
        expectedCommits.add(new Commit("ab12cd34ef56ab78cd90ef12ab34cd56ef7890ab", "README.md rewritten with Arcane Editor 4", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:10:01Z"))));
        expectedCommits.add(new Commit("ccee1122ddee3344ff5566778899aabbccddeeff", "README.md rewritten with Arcane Editor 3", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:09:41Z"))));
        expectedCommits.add(new Commit("dd44ee55cc66bb77aa8899ccddaa55bbccddeeff", "README.md rewritten with Arcane Editor", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:09:22Z"))));

        // Then
        // Verify
        assertThat(commits).isNotNull();
        assertThat(commits.getContent()).isNotEmpty();
        assertThat(commits.getContent().size()).isEqualTo(commits.getContent().size());
        assertThat(commits.getContent())
                .usingRecursiveComparison()
                .isEqualTo(expectedCommits);

        Map<String, Object> queryParams = Map.of(
                "ownerId", "test-user",
                "repoId", "test-repo",
                "from", "v1.0.0",
                "to", "v2.0.0",
                "pagelen", 20,
                "page", 1
        );

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}&exclude={from}&include={to}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
                eq(queryParams)
        );
    }

    @Test
    void whenListCommitsCalledWithOnlyFromTagFilterThenAssertCommitsReturned() throws Exception{
        // Given
        BitbucketListCommitsCommitListRes filteredCommitsRes = loadJson("bitbucket/list_commits_filtered.json", BitbucketListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo");
        repository.setOwnerId("test-user");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters("v1.0.0", null, null, null, null, null);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}&exclude={from}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(filteredCommitsRes, HttpStatus.OK));

        // When
        Page<Commit> commits = bitbucketProvider.listCommits(repository, filters, pageable);

        List<Commit> expectedCommits = new ArrayList<>();
        expectedCommits.add(new Commit("ab12cd34ef56ab78cd90ef12ab34cd56ef7890ab", "README.md rewritten with Arcane Editor 4", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:10:01Z"))));
        expectedCommits.add(new Commit("ccee1122ddee3344ff5566778899aabbccddeeff", "README.md rewritten with Arcane Editor 3", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:09:41Z"))));
        expectedCommits.add(new Commit("dd44ee55cc66bb77aa8899ccddaa55bbccddeeff", "README.md rewritten with Arcane Editor", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:09:22Z"))));

        // Then
        // Verify
        assertThat(commits).isNotNull();
        assertThat(commits.getContent()).isNotEmpty();
        assertThat(commits.getContent().size()).isEqualTo(commits.getContent().size());
        assertThat(commits.getContent())
                .usingRecursiveComparison()
                .isEqualTo(expectedCommits);

        Map<String, Object> queryParams = Map.of(
                "ownerId", "test-user",
                "repoId", "test-repo",
                "from", "v1.0.0",
                "pagelen", 20,
                "page", 1
        );

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}&exclude={from}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
                eq(queryParams)
        );
    }

    @Test
    void whenListCommitsCalledWithOnlyToTagFilterThenAssertCommitsReturned() throws Exception{
        // Given
        BitbucketListCommitsCommitListRes filteredCommitsRes = loadJson("bitbucket/list_commits_filtered.json", BitbucketListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo");
        repository.setOwnerId("test-user");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        ListCommitFilters filters = new ListCommitFilters(null, "v1.0.0", null, null, null, null);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}&include={to}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(filteredCommitsRes, HttpStatus.OK));

        // When
        Page<Commit> commits = bitbucketProvider.listCommits(repository, filters, pageable);

        List<Commit> expectedCommits = new ArrayList<>();
        expectedCommits.add(new Commit("ab12cd34ef56ab78cd90ef12ab34cd56ef7890ab", "README.md rewritten with Arcane Editor 4", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:10:01Z"))));
        expectedCommits.add(new Commit("ccee1122ddee3344ff5566778899aabbccddeeff", "README.md rewritten with Arcane Editor 3", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:09:41Z"))));
        expectedCommits.add(new Commit("dd44ee55cc66bb77aa8899ccddaa55bbccddeeff", "README.md rewritten with Arcane Editor", "900001:aaaa1111-bbbb-2222-cccc-333333dddddd", Date.from(Instant.parse("2025-12-03T15:09:22Z"))));

        // Then
        // Verify
        assertThat(commits).isNotNull();
        assertThat(commits.getContent()).isNotEmpty();
        assertThat(commits.getContent().size()).isEqualTo(commits.getContent().size());
        assertThat(commits.getContent())
                .usingRecursiveComparison()
                .isEqualTo(expectedCommits);

        Map<String, Object> queryParams = Map.of(
                "ownerId", "test-user",
                "repoId", "test-repo",
                "to", "v1.0.0",
                "pagelen", 20,
                "page", 1
        );

        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{ownerId}/{repoId}/commits?page={page}&pagelen={pagelen}&include={to}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class),
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
        assertThatThrownBy(() -> bitbucketProvider.listCommits(
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
        assertThatThrownBy(() -> bitbucketProvider.listCommits(
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


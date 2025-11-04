package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
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
import java.util.Optional;

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
    private PatCredential credential;
    private String baseUrl = "https://api.bitbucket.org/2.0";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        credential = new PatCredential("test-user", "test-token");
        bitbucketProvider = new BitbucketProvider(baseUrl, restTemplate, credential);
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
                contains("/workspaces"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class)
        )).thenReturn(new ResponseEntity<>(workspacesRes, HttpStatus.OK));

        // Test
        Page<Organization> organizations = bitbucketProvider.listOrganizations(pageable);

        // Verify
        assertThat(organizations).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/workspaces"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class)
        );
    }

    @Test
    void whenGetOrganizationCalledThenAssertOrganizationReturned() throws Exception {
        // Load JSON response
        BitbucketGetOrganizationWorkspaceRes workspaceRes = loadJson("bitbucket/get_organization.json", BitbucketGetOrganizationWorkspaceRes.class);
        String workspaceSlug = "test-org";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/workspaces/" + workspaceSlug),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetOrganizationWorkspaceRes.class)
        )).thenReturn(new ResponseEntity<>(workspaceRes, HttpStatus.OK));

        // Test
        Optional<Organization> organization = bitbucketProvider.getOrganization(workspaceSlug);

        // Verify
        assertThat(organization).isPresent();
        assertThat(organization.get().getName()).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetOrganizationWorkspaceRes.class)
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
                contains("/workspaces/test-org/members"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListMembersUserListRes.class)
        )).thenReturn(new ResponseEntity<>(membersRes, HttpStatus.OK));

        // Test
        Page<User> members = bitbucketProvider.listMembers(org, pageable);

        // Verify
        assertThat(members).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/workspaces/test-org/members"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListMembersUserListRes.class)
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
        String repoId = "test-user/test-repo";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/" + repoId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetRepositoryRepositoryRes.class)
        )).thenReturn(new ResponseEntity<>(repoRes, HttpStatus.OK));

        // Test
        Optional<Repository> repository = bitbucketProvider.getRepository(repoId);

        // Verify
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/" + repoId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketGetRepositoryRepositoryRes.class)
        );
    }

    @Test
    void whenListCommitsCalledThenAssertCommitsReturned() throws Exception {
        // Load JSON response
        BitbucketListCommitsCommitListRes commitsRes = loadJson("bitbucket/list_commits.json", BitbucketListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        Organization org = new Organization("test-user", "test-user", "https://bitbucket.org/test-user");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/repositories/test-user/test-repo/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class)
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = bitbucketProvider.listCommits(org, null, repository, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/repositories/test-user/test-repo/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListCommitsCommitListRes.class)
        );
    }

    @Test
    void whenListBranchesCalledThenAssertBranchesReturned() throws Exception {
        // Load JSON response
        BitbucketListBranchesBranchListRes branchesRes = loadJson("bitbucket/list_branches.json", BitbucketListBranchesBranchListRes.class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        Organization org = new Organization("test-user", "test-user", "https://bitbucket.org/test-user");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/repositories/test-user/test-repo/refs/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListBranchesBranchListRes.class)
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = bitbucketProvider.listBranches(org, null, repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/repositories/test-user/test-repo/refs/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListBranchesBranchListRes.class)
        );
    }

    @Test
    void whenListTagsCalledThenAssertTagsReturned() throws Exception {
        // Load JSON response
        BitbucketListTagsTagListRes tagsRes = loadJson("bitbucket/list_tags.json", BitbucketListTagsTagListRes.class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        Organization org = new Organization("test-user", "test-user", "https://bitbucket.org/test-user");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/repositories/test-user/test-repo/refs/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListTagsTagListRes.class)
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = bitbucketProvider.listTags(org, null, repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/repositories/test-user/test-repo/refs/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListTagsTagListRes.class)
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
                eq(baseUrl + "/workspaces/test-org/projects?page=1&pagelen=20"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class)
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        // Test
        Page<ProviderCustomResource> customResources = bitbucketProvider.getProviderCustomResources("project", parameters, pageable);

        // Verify
        assertThat(customResources).isNotNull();
        assertThat(customResources.getContent()).isNotEmpty();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/workspaces/test-org/projects?page=1&pagelen=20"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class)
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
                contains("/workspaces?page="),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class)
        )).thenReturn(new ResponseEntity<>(workspacesRes, HttpStatus.OK));
        
        when(restTemplate.exchange(
                contains("/workspaces/Test Org/projects?page=1&pagelen=100"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class)
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        // Test
        Page<ProviderCustomResource> customResources = bitbucketProvider.getProviderCustomResources("project", parameters, pageable);

        // Verify
        assertThat(customResources).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                contains("/workspaces?page="),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListOrganizationsWorkspaceListRes.class)
        );
        verify(restTemplate, atLeastOnce()).exchange(
                contains("/workspaces/Test Org/projects"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BitbucketListProjectsProjectListRes.class)
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


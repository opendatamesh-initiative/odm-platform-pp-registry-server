package org.opendatamesh.platform.pp.registry.githandler.provider.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.checkconnection.GitHubCheckConnectionUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getcurrentuser.GitHubGetCurrentUserUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getorganization.GitHubGetOrganizationOrganizationRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getrepository.GitHubGetRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listbranches.GitHubListBranchesBranchRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits.GitHubListCommitsCommitRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listmembers.GitHubListMembersUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listorganizations.GitHubListOrganizationsOrganizationRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listrepositories.GitHubListRepositoriesRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listtags.GitHubListTagsTagRes;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubProviderTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private GitHubProvider gitHubProvider;
    private PatCredential credential;
    private String baseUrl = "https://api.github.com";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        credential = new PatCredential("test-user", "test-token");
        gitHubProvider = new GitHubProvider(baseUrl, restTemplate, credential);
    }

    @Test
    void whenCheckConnectionCalledThenAssertConnectionSuccessful() throws Exception {
        // Load JSON response
        GitHubCheckConnectionUserRes userRes = loadJson("github/get_current_user.json", GitHubCheckConnectionUserRes.class);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubCheckConnectionUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        gitHubProvider.checkConnection();

        // Verify
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubCheckConnectionUserRes.class)
        );
    }

    @Test
    void whenGetCurrentUserCalledThenAssertUserReturned() throws Exception {
        // Load JSON response
        GitHubGetCurrentUserUserRes userRes = loadJson("github/get_current_user.json", GitHubGetCurrentUserUserRes.class);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetCurrentUserUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        User user = gitHubProvider.getCurrentUser();

        // Verify
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetCurrentUserUserRes.class)
        );
    }

    @Test
    void whenListOrganizationsCalledThenAssertOrganizationsReturned() throws Exception {
        // Load JSON response
        GitHubListOrganizationsOrganizationRes[] orgsRes = loadJson("github/list_organizations.json", GitHubListOrganizationsOrganizationRes[].class);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/user/orgs?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListOrganizationsOrganizationRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(orgsRes, HttpStatus.OK));

        // Test
        Page<Organization> organizations = gitHubProvider.listOrganizations(pageable);

        // Verify
        assertThat(organizations).isNotNull();
        assertThat(organizations.getContent()).isNotEmpty();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/user/orgs?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListOrganizationsOrganizationRes[].class),
                anyMap()
        );
    }

    @Test
    void whenGetOrganizationCalledThenAssertOrganizationReturned() throws Exception {
        // Load JSON response
        GitHubGetOrganizationOrganizationRes orgRes = loadJson("github/get_organization.json", GitHubGetOrganizationOrganizationRes.class);
        String orgId = "test-org";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/orgs/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetOrganizationOrganizationRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(orgRes, HttpStatus.OK));

        // Test
        Optional<Organization> organization = gitHubProvider.getOrganization(orgId);

        // Verify
        assertThat(organization).isPresent();
        assertThat(organization.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/orgs/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetOrganizationOrganizationRes.class),
                anyMap()
        );
    }

    @Test
    void whenListMembersCalledThenAssertMembersReturned() throws Exception {
        // Load JSON response
        GitHubListMembersUserRes[] membersRes = loadJson("github/list_members.json", GitHubListMembersUserRes[].class);
        Organization org = new Organization("test-org", "test-org", "https://github.com/test-org");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/orgs/{orgName}/members?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListMembersUserRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(membersRes, HttpStatus.OK));

        // Test
        Page<User> members = gitHubProvider.listMembers(org, pageable);

        // Verify
        assertThat(members).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/orgs/{orgName}/members?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListMembersUserRes[].class),
                anyMap()
        );
    }

    @Test
    void whenListRepositoriesCalledThenAssertRepositoriesReturned() throws Exception {
        // Load JSON response
        GitHubListRepositoriesRepositoryRes[] reposRes = loadJson("github/list_repositories.json", GitHubListRepositoriesRepositoryRes[].class);
        Pageable pageable = PageRequest.of(0, 20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/user/repos?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListRepositoriesRepositoryRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(reposRes, HttpStatus.OK));

        // Test
        Page<Repository> repositories = gitHubProvider.listRepositories(null, null, parameters, pageable);

        // Verify
        assertThat(repositories).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/user/repos?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListRepositoriesRepositoryRes[].class),
                anyMap()
        );
    }

    @Test
    void whenGetRepositoryCalledThenAssertRepositoryReturned() throws Exception {
        // Load JSON response
        GitHubGetRepositoryRepositoryRes repoRes = loadJson("github/get_repository.json", GitHubGetRepositoryRepositoryRes.class);
        String repoId = "342219496";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetRepositoryRepositoryRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(repoRes, HttpStatus.OK));

        // Test
        Optional<Repository> repository = gitHubProvider.getRepository(repoId, null);

        // Verify
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetRepositoryRepositoryRes.class),
                anyMap()
        );
    }

    @Test
    void whenListCommitsCalledThenAssertCommitsReturned() throws Exception {
        // Load JSON responses
        GitHubGetOrganizationOrganizationRes orgRes = loadJson("github/get_organization.json", GitHubGetOrganizationOrganizationRes.class);
        GitHubListCommitsCommitRes[] commitsRes = loadJson("github/list_commits.json", GitHubListCommitsCommitRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setId("342219496");
        repository.setOwnerId("test-org");
        repository.setOwnerType(OwnerType.ORGANIZATION);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock getOrganization call (called internally by listCommits)
        when(restTemplate.exchange(
                eq(baseUrl + "/orgs/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetOrganizationOrganizationRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(orgRes, HttpStatus.OK));
        
        // Mock RestTemplate response for listCommits
        when(restTemplate.exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/commits?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListCommitsCommitRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = gitHubProvider.listCommits(repository, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/commits?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListCommitsCommitRes[].class),
                anyMap()
        );
    }

    @Test
    void whenListCommitsCalledWithAccountOwnerThenAssertCommitsReturned() throws Exception {
        // Load JSON responses
        GitHubGetCurrentUserUserRes userRes = loadJson("github/get_current_user.json", GitHubGetCurrentUserUserRes.class);
        GitHubListCommitsCommitRes[] commitsRes = loadJson("github/list_commits.json", GitHubListCommitsCommitRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setId("342219496");
        repository.setOwnerId("test-org");
        repository.setOwnerType(OwnerType.ACCOUNT);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock getCurrentUser call (called internally by listCommits)
        when(restTemplate.exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetCurrentUserUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));
        
        // Mock RestTemplate response for listCommits
        when(restTemplate.exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/commits?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListCommitsCommitRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = gitHubProvider.listCommits(repository, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/commits?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListCommitsCommitRes[].class),
                anyMap()
        );
    }

    @Test
    void whenListBranchesCalledThenAssertBranchesReturned() throws Exception {
        // Load JSON responses
        GitHubGetOrganizationOrganizationRes orgRes = loadJson("github/get_organization.json", GitHubGetOrganizationOrganizationRes.class);
        GitHubListBranchesBranchRes[] branchesRes = loadJson("github/list_branches.json", GitHubListBranchesBranchRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setOwnerId("test-org");
        repository.setOwnerType(OwnerType.ORGANIZATION);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock getOrganization call (called internally by listBranches)
        when(restTemplate.exchange(
                eq(baseUrl + "/orgs/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetOrganizationOrganizationRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(orgRes, HttpStatus.OK));
        
        // Mock RestTemplate response for listBranches
        when(restTemplate.exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/branches?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListBranchesBranchRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = gitHubProvider.listBranches(repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/branches?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListBranchesBranchRes[].class),
                anyMap()
        );
    }

    @Test
    void whenListBranchesCalledWithAccountOwnerThenAssertBranchesReturned() throws Exception {
        // Load JSON responses
        GitHubGetCurrentUserUserRes userRes = loadJson("github/get_current_user.json", GitHubGetCurrentUserUserRes.class);
        GitHubListBranchesBranchRes[] branchesRes = loadJson("github/list_branches.json", GitHubListBranchesBranchRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setOwnerId("test-org");
        repository.setOwnerType(OwnerType.ACCOUNT);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock getCurrentUser call (called internally by listBranches)
        when(restTemplate.exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetCurrentUserUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));
        
        // Mock RestTemplate response for listBranches
        when(restTemplate.exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/branches?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListBranchesBranchRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = gitHubProvider.listBranches(repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/branches?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListBranchesBranchRes[].class),
                anyMap()
        );
    }


    @Test
    void whenListTagsCalledThenAssertTagsReturned() throws Exception {
        // Load JSON responses
        GitHubGetOrganizationOrganizationRes orgRes = loadJson("github/get_organization.json", GitHubGetOrganizationOrganizationRes.class);
        GitHubListTagsTagRes[] tagsRes = loadJson("github/list_tags.json", GitHubListTagsTagRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setOwnerId("test-org");
        repository.setOwnerType(OwnerType.ORGANIZATION);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock getOrganization call (called internally by listTags)
        when(restTemplate.exchange(
                eq(baseUrl + "/orgs/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetOrganizationOrganizationRes.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(orgRes, HttpStatus.OK));
        
        // Mock RestTemplate response for listTags
        when(restTemplate.exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/tags?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListTagsTagRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = gitHubProvider.listTags(repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/tags?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListTagsTagRes[].class),
                anyMap()
        );
    }

    @Test
    void whenListTagsCalledWithAccountOwnerThenAssertTagsReturned() throws Exception {
        // Load JSON responses
        GitHubGetCurrentUserUserRes userRes = loadJson("github/get_current_user.json", GitHubGetCurrentUserUserRes.class);
        GitHubListTagsTagRes[] tagsRes = loadJson("github/list_tags.json", GitHubListTagsTagRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setOwnerId("test-org");
        repository.setOwnerType(OwnerType.ACCOUNT);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock getCurrentUser call (called internally by listTags)
        when(restTemplate.exchange(
                eq(baseUrl + "/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetCurrentUserUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));
        
        // Mock RestTemplate response for listTags
        when(restTemplate.exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/tags?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListTagsTagRes[].class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = gitHubProvider.listTags(repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repos/{owner}/{repo}/tags?page={page}&per_page={perPage}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListTagsTagRes[].class),
                anyMap()
        );
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


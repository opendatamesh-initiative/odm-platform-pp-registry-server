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
                contains("/user/orgs"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListOrganizationsOrganizationRes[].class)
        )).thenReturn(new ResponseEntity<>(orgsRes, HttpStatus.OK));

        // Test
        Page<Organization> organizations = gitHubProvider.listOrganizations(pageable);

        // Verify
        assertThat(organizations).isNotNull();
        assertThat(organizations.getContent()).isNotEmpty();
        verify(restTemplate, times(1)).exchange(
                contains("/user/orgs"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListOrganizationsOrganizationRes[].class)
        );
    }

    @Test
    void whenGetOrganizationCalledThenAssertOrganizationReturned() throws Exception {
        // Load JSON response
        GitHubGetOrganizationOrganizationRes orgRes = loadJson("github/get_organization.json", GitHubGetOrganizationOrganizationRes.class);
        String orgId = "test-org";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/orgs/" + orgId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetOrganizationOrganizationRes.class)
        )).thenReturn(new ResponseEntity<>(orgRes, HttpStatus.OK));

        // Test
        Optional<Organization> organization = gitHubProvider.getOrganization(orgId);

        // Verify
        assertThat(organization).isPresent();
        assertThat(organization.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/orgs/" + orgId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetOrganizationOrganizationRes.class)
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
                contains("/orgs/test-org/members"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListMembersUserRes[].class)
        )).thenReturn(new ResponseEntity<>(membersRes, HttpStatus.OK));

        // Test
        Page<User> members = gitHubProvider.listMembers(org, pageable);

        // Verify
        assertThat(members).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/orgs/test-org/members"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListMembersUserRes[].class)
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
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListRepositoriesRepositoryRes[].class)
        )).thenReturn(new ResponseEntity<>(reposRes, HttpStatus.OK));

        // Test
        Page<Repository> repositories = gitHubProvider.listRepositories(null, null, parameters, pageable);

        // Verify
        assertThat(repositories).isNotNull();
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListRepositoriesRepositoryRes[].class)
        );
    }

    @Test
    void whenGetRepositoryCalledThenAssertRepositoryReturned() throws Exception {
        // Load JSON response
        GitHubGetRepositoryRepositoryRes repoRes = loadJson("github/get_repository.json", GitHubGetRepositoryRepositoryRes.class);
        String repoId = "342219496";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/repositories/" + repoId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetRepositoryRepositoryRes.class)
        )).thenReturn(new ResponseEntity<>(repoRes, HttpStatus.OK));

        // Test
        Optional<Repository> repository = gitHubProvider.getRepository(repoId);

        // Verify
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/repositories/" + repoId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubGetRepositoryRepositoryRes.class)
        );
    }

    @Test
    void whenListCommitsCalledThenAssertCommitsReturned() throws Exception {
        // Load JSON response
        GitHubListCommitsCommitRes[] commitsRes = loadJson("github/list_commits.json", GitHubListCommitsCommitRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setId("342219496");
        Organization org = new Organization("test-org", "test-org", "https://github.com/test-org");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/repos/test-org/test-repo/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListCommitsCommitRes[].class)
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = gitHubProvider.listCommits(org, null, repository, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/repos/test-org/test-repo/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListCommitsCommitRes[].class)
        );
    }

    @Test
    void whenListBranchesCalledThenAssertBranchesReturned() throws Exception {
        // Load JSON response
        GitHubListBranchesBranchRes[] branchesRes = loadJson("github/list_branches.json", GitHubListBranchesBranchRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        Organization org = new Organization("test-org", "test-org", "https://github.com/test-org");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/repos/test-org/test-repo/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListBranchesBranchRes[].class)
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = gitHubProvider.listBranches(org, null, repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/repos/test-org/test-repo/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListBranchesBranchRes[].class)
        );
    }

    @Test
    void whenListTagsCalledThenAssertTagsReturned() throws Exception {
        // Load JSON response
        GitHubListTagsTagRes[] tagsRes = loadJson("github/list_tags.json", GitHubListTagsTagRes[].class);
        Repository repository = new Repository();
        repository.setName("test-repo");
        Organization org = new Organization("test-org", "test-org", "https://github.com/test-org");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/repos/test-org/test-repo/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListTagsTagRes[].class)
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = gitHubProvider.listTags(org, null, repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/repos/test-org/test-repo/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubListTagsTagRes[].class)
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


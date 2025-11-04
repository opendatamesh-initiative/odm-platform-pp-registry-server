package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.checkconnection.GitLabCheckConnectionUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getcurrentuser.GitLabGetCurrentUserUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getorganization.GitLabGetOrganizationGroupRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getrepository.GitLabGetRepositoryProjectRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listbranches.GitLabListBranchesBranchRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listcommits.GitLabListCommitsCommitRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listmembers.GitLabListMembersUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listorganizations.GitLabListOrganizationsGroupRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listrepositories.GitLabListRepositoriesProjectRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listtags.GitLabListTagsTagRes;
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
class GitLabProviderTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private GitLabProvider gitLabProvider;
    private PatCredential credential;
    private String baseUrl = "https://gitlab.com";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        credential = new PatCredential("test-user", "test-token");
        gitLabProvider = new GitLabProvider(baseUrl, restTemplate, credential);
    }

    @Test
    void whenCheckConnectionCalledThenAssertConnectionSuccessful() throws Exception {
        // Load JSON response
        GitLabCheckConnectionUserRes userRes = loadJson("gitlab/get_current_user.json", GitLabCheckConnectionUserRes.class);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/api/v4/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabCheckConnectionUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        gitLabProvider.checkConnection();

        // Verify
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/api/v4/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabCheckConnectionUserRes.class)
        );
    }

    @Test
    void whenGetCurrentUserCalledThenAssertUserReturned() throws Exception {
        // Load JSON response
        GitLabGetCurrentUserUserRes userRes = loadJson("gitlab/get_current_user.json", GitLabGetCurrentUserUserRes.class);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/api/v4/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabGetCurrentUserUserRes.class)
        )).thenReturn(new ResponseEntity<>(userRes, HttpStatus.OK));

        // Test
        User user = gitLabProvider.getCurrentUser();

        // Verify
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/api/v4/user"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabGetCurrentUserUserRes.class)
        );
    }

    @Test
    void whenListOrganizationsCalledThenAssertOrganizationsReturned() throws Exception {
        // Load JSON response
        GitLabListOrganizationsGroupRes[] groupsRes = loadJson("gitlab/list_organizations.json", GitLabListOrganizationsGroupRes[].class);
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/api/v4/groups"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListOrganizationsGroupRes[].class)
        )).thenReturn(new ResponseEntity<>(groupsRes, HttpStatus.OK));

        // Test
        Page<Organization> organizations = gitLabProvider.listOrganizations(pageable);

        // Verify
        assertThat(organizations).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/api/v4/groups"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListOrganizationsGroupRes[].class)
        );
    }

    @Test
    void whenGetOrganizationCalledThenAssertOrganizationReturned() throws Exception {
        // Load JSON response
        GitLabGetOrganizationGroupRes groupRes = loadJson("gitlab/get_organization.json", GitLabGetOrganizationGroupRes.class);
        String groupId = "115743511";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/api/v4/groups/" + groupId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabGetOrganizationGroupRes.class)
        )).thenReturn(new ResponseEntity<>(groupRes, HttpStatus.OK));

        // Test
        Optional<Organization> organization = gitLabProvider.getOrganization(groupId);

        // Verify
        assertThat(organization).isPresent();
        assertThat(organization.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/api/v4/groups/" + groupId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabGetOrganizationGroupRes.class)
        );
    }

    @Test
    void whenListMembersCalledThenAssertMembersReturned() throws Exception {
        // Load JSON response
        GitLabListMembersUserRes[] membersRes = loadJson("gitlab/list_members.json", GitLabListMembersUserRes[].class);
        Organization org = new Organization("115743511", "test-org", "https://gitlab.com/groups/test-org");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/api/v4/groups/" + org.getId() + "/members"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListMembersUserRes[].class)
        )).thenReturn(new ResponseEntity<>(membersRes, HttpStatus.OK));

        // Test
        Page<User> members = gitLabProvider.listMembers(org, pageable);

        // Verify
        assertThat(members).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/api/v4/groups/" + org.getId() + "/members"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListMembersUserRes[].class)
        );
    }

    @Test
    void whenListRepositoriesCalledThenAssertRepositoriesReturned() throws Exception {
        // Load JSON response
        GitLabListRepositoriesProjectRes[] projectsRes = loadJson("gitlab/list_repositories.json", GitLabListRepositoriesProjectRes[].class);
        Pageable pageable = PageRequest.of(0, 20);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        User user = new User("123", "test-user", "Test User", null, null);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListRepositoriesProjectRes[].class)
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        // Test
        Page<Repository> repositories = gitLabProvider.listRepositories(null, user, parameters, pageable);

        // Verify
        assertThat(repositories).isNotNull();
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListRepositoriesProjectRes[].class)
        );
    }

    @Test
    void whenGetRepositoryCalledThenAssertRepositoryReturned() throws Exception {
        // Load JSON response
        GitLabGetRepositoryProjectRes projectRes = loadJson("gitlab/get_repository.json", GitLabGetRepositoryProjectRes.class);
        String projectId = "75825589";
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                eq(baseUrl + "/api/v4/projects/" + projectId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabGetRepositoryProjectRes.class)
        )).thenReturn(new ResponseEntity<>(projectRes, HttpStatus.OK));

        // Test
        Optional<Repository> repository = gitLabProvider.getRepository(projectId);

        // Verify
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isNotNull();
        verify(restTemplate, times(1)).exchange(
                eq(baseUrl + "/api/v4/projects/" + projectId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabGetRepositoryProjectRes.class)
        );
    }

    @Test
    void whenListCommitsCalledThenAssertCommitsReturned() throws Exception {
        // Load JSON response
        GitLabListCommitsCommitRes[] commitsRes = loadJson("gitlab/list_commits.json", GitLabListCommitsCommitRes[].class);
        Repository repository = new Repository();
        repository.setId("75825589");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/api/v4/projects/75825589/repository/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListCommitsCommitRes[].class)
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = gitLabProvider.listCommits(null, null, repository, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/api/v4/projects/75825589/repository/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListCommitsCommitRes[].class)
        );
    }

    @Test
    void whenListBranchesCalledThenAssertBranchesReturned() throws Exception {
        // Load JSON response
        GitLabListBranchesBranchRes[] branchesRes = loadJson("gitlab/list_branches.json", GitLabListBranchesBranchRes[].class);
        Repository repository = new Repository();
        repository.setId("75825589");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/api/v4/projects/75825589/repository/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListBranchesBranchRes[].class)
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = gitLabProvider.listBranches(null, null, repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/api/v4/projects/75825589/repository/branches"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListBranchesBranchRes[].class)
        );
    }

    @Test
    void whenListTagsCalledThenAssertTagsReturned() throws Exception {
        // Load JSON response
        GitLabListTagsTagRes[] tagsRes = loadJson("gitlab/list_tags.json", GitLabListTagsTagRes[].class);
        Repository repository = new Repository();
        repository.setId("75825589");
        repository.setName("test-repo");
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/api/v4/projects/75825589/repository/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListTagsTagRes[].class)
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = gitLabProvider.listTags(null, null, repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/api/v4/projects/75825589/repository/tags"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitLabListTagsTagRes[].class)
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


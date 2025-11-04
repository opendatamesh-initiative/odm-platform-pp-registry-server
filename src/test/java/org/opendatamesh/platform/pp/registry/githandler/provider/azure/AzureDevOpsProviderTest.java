package org.opendatamesh.platform.pp.registry.githandler.provider.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureDevOpsProviderTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private AzureDevOpsProvider azureDevOpsProvider;
    private PatCredential credential;
    private String baseUrl = "https://dev.azure.com";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        credential = new PatCredential("test-user", "test-token");
        azureDevOpsProvider = new AzureDevOpsProvider(baseUrl, restTemplate, credential);
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
                contains("/_apis/projects"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListRepositoriesProjectListRes.class)
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        when(restTemplate.exchange(
                contains("/_apis/git/repositories"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListRepositoriesRepositoryListRes.class)
        )).thenReturn(new ResponseEntity<>(reposRes, HttpStatus.OK));

        // Test
        Page<Repository> repositories = azureDevOpsProvider.listRepositories(null, null, parameters, pageable);

        // Verify
        assertThat(repositories).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                contains("/_apis/projects"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListRepositoriesProjectListRes.class)
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
                contains("/_apis/projects"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryProjectListRes.class)
        )).thenReturn(new ResponseEntity<>(projectsRes, HttpStatus.OK));

        when(restTemplate.exchange(
                contains("/_apis/git/repositories/" + repoId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryRepositoryRes.class)
        )).thenReturn(new ResponseEntity<>(repoRes, HttpStatus.OK));

        // Test
        Optional<Repository> repository = azureDevOpsProvider.getRepository(repoId);

        // Verify
        assertThat(repository).isPresent();
        assertThat(repository.get().getName()).isNotNull();
        verify(restTemplate, atLeastOnce()).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryProjectListRes.class)
        );
        verify(restTemplate, atLeastOnce()).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureGetRepositoryRepositoryRes.class)
        );
    }

    @Test
    void whenListCommitsCalledThenAssertCommitsReturned() throws Exception {
        // Load JSON response
        AzureListCommitsCommitListRes commitsRes = loadJson("azure/list_commits.json", AzureListCommitsCommitListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        Organization org = new Organization("default-org", "default-org", baseUrl);
        Pageable pageable = PageRequest.of(0, 20);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/_apis/git/repositories/" + repository.getId() + "/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class)
        )).thenReturn(new ResponseEntity<>(commitsRes, HttpStatus.OK));

        // Test
        Page<Commit> commits = azureDevOpsProvider.listCommits(org, null, repository, pageable);

        // Verify
        assertThat(commits).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/_apis/git/repositories/" + repository.getId() + "/commits"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListCommitsCommitListRes.class)
        );
    }

    @Test
    void whenListBranchesCalledThenAssertBranchesReturned() throws Exception {
        // Load JSON response
        AzureListBranchesBranchListRes branchesRes = loadJson("azure/list_branches.json", AzureListBranchesBranchListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        Organization org = new Organization("default-org", "default-org", baseUrl);
        Pageable pageable = PageRequest.of(0, 20);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/_apis/git/repositories/" + repository.getId() + "/refs"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListBranchesBranchListRes.class)
        )).thenReturn(new ResponseEntity<>(branchesRes, HttpStatus.OK));

        // Test
        Page<Branch> branches = azureDevOpsProvider.listBranches(org, null, repository, pageable);

        // Verify
        assertThat(branches).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/_apis/git/repositories/" + repository.getId() + "/refs"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListBranchesBranchListRes.class)
        );
    }

    @Test
    void whenListTagsCalledThenAssertTagsReturned() throws Exception {
        // Load JSON response
        AzureListTagsTagListRes tagsRes = loadJson("azure/list_tags.json", AzureListTagsTagListRes.class);
        Repository repository = new Repository();
        repository.setId("test-repo-id");
        Organization org = new Organization("default-org", "default-org", baseUrl);
        Pageable pageable = PageRequest.of(0, 20);

        // Mock RestTemplate response
        when(restTemplate.exchange(
                contains("/_apis/git/repositories/" + repository.getId() + "/refs"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListTagsTagListRes.class)
        )).thenReturn(new ResponseEntity<>(tagsRes, HttpStatus.OK));

        // Test
        Page<Tag> tags = azureDevOpsProvider.listTags(org, null, repository, pageable);

        // Verify
        assertThat(tags).isNotNull();
        verify(restTemplate, times(1)).exchange(
                contains("/_apis/git/repositories/" + repository.getId() + "/refs"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AzureListTagsTagListRes.class)
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


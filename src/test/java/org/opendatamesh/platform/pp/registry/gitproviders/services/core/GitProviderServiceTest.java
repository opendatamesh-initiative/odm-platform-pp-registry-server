package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.Organization;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.User;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitProviderServiceTest {

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private RepositoryMapper repositoryMapper;

    @Mock
    private GitProviderFactory gitProviderFactory;

    @Mock
    private GitProvider gitProvider;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GitProviderServiceImpl gitProviderService;

    private PatCredential testCredential;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testCredential = new PatCredential("test-user", "test-token");
        testPageable = PageRequest.of(0, 10);
    }

    @Test
    void whenListOrganizationsWithValidProviderThenReturnOrganizations() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        
        Organization mockOrg1 = createMockOrganization("123", "test-org-1");
        Organization mockOrg2 = createMockOrganization("456", "test-org-2");
        List<Organization> mockOrganizations = Arrays.asList(mockOrg1, mockOrg2);
        Page<Organization> mockPage = new PageImpl<>(mockOrganizations, testPageable, 2);

        OrganizationRes mockOrgRes1 = createMockOrganizationRes("123", "test-org-1");
        OrganizationRes mockOrgRes2 = createMockOrganizationRes("456", "test-org-2");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(gitProvider);
        
        when(gitProvider.listOrganizations(testPageable)).thenReturn(mockPage);
        when(organizationMapper.toRes(any(Organization.class))).thenReturn(mockOrgRes1, mockOrgRes2);

        // When
        Page<OrganizationRes> result = gitProviderService.listOrganizations(
                providerType, providerBaseUrl, testPageable, testCredential
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(gitProviderFactory).getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        );
        verify(gitProvider).listOrganizations(testPageable);
        verify(organizationMapper, times(2)).toRes(any(Organization.class));
    }


    @Test
    void whenListRepositoriesWithValidParametersThenReturnRepositories() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";
        String organizationId = "456";
        String organizationName = "testorg";

        Repository mockRepo1 = createMockRepository("repo1", "Test Repository 1");
        Repository mockRepo2 = createMockRepository("repo2", "Test Repository 2");
        List<Repository> mockRepositories = Arrays.asList(mockRepo1, mockRepo2);
        Page<Repository> mockPage = new PageImpl<>(mockRepositories, testPageable, 2);

        RepositoryRes mockRepoRes1 = createMockRepositoryRes("repo1", "Test Repository 1");
        RepositoryRes mockRepoRes2 = createMockRepositoryRes("repo2", "Test Repository 2");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(gitProvider);
        
        when(gitProvider.listRepositories(any(Organization.class), any(User.class), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(any(Repository.class))).thenReturn(mockRepoRes1, mockRepoRes2);

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerType, providerBaseUrl, userId, username, organizationId, organizationName, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(gitProviderFactory).getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        );
        verify(gitProvider).listRepositories(any(Organization.class), any(User.class), eq(testPageable));
        verify(repositoryMapper, times(2)).toRes(any(Repository.class));
    }

    @Test
    void whenListRepositoriesWithoutOrganizationThenCreateUserOnlyRequest() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";
        String organizationId = null;
        String organizationName = null;

        Repository mockRepo = createMockRepository("user-repo", "User Repository");
        Page<Repository> mockPage = new PageImpl<>(Arrays.asList(mockRepo), testPageable, 1);
        RepositoryRes mockRepoRes = createMockRepositoryRes("user-repo", "User Repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(gitProvider);
        
        when(gitProvider.listRepositories(eq(null), any(User.class), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(mockRepo)).thenReturn(mockRepoRes);

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerType, providerBaseUrl, userId, username, organizationId, organizationName, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockRepoRes);

        verify(gitProvider).listRepositories(eq(null), any(User.class), eq(testPageable));
    }

    @Test
    void whenListRepositoriesWithEmptyOrganizationIdThenCreateUserOnlyRequest() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";
        String organizationId = "";
        String organizationName = "testorg";

        Repository mockRepo = createMockRepository("user-repo", "User Repository");
        Page<Repository> mockPage = new PageImpl<>(Arrays.asList(mockRepo), testPageable, 1);
        RepositoryRes mockRepoRes = createMockRepositoryRes("user-repo", "User Repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(gitProvider);
        
        when(gitProvider.listRepositories(eq(null), any(User.class), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(mockRepo)).thenReturn(mockRepoRes);

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerType, providerBaseUrl, userId, username, organizationId, organizationName, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockRepoRes);

        verify(gitProvider).listRepositories(eq(null), any(User.class), eq(testPageable));
    }

    @Test
    void whenListRepositoriesWithOrganizationNameOnlyThenUseOrganizationName() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";
        String organizationId = "456";
        String organizationName = null; // null organization name

        Repository mockRepo = createMockRepository("org-repo", "Organization Repository");
        Page<Repository> mockPage = new PageImpl<>(Arrays.asList(mockRepo), testPageable, 1);
        RepositoryRes mockRepoRes = createMockRepositoryRes("org-repo", "Organization Repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(gitProvider);
        
        when(gitProvider.listRepositories(any(Organization.class), any(User.class), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(mockRepo)).thenReturn(mockRepoRes);

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerType, providerBaseUrl, userId, username, organizationId, organizationName, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockRepoRes);

        verify(gitProvider).listRepositories(any(Organization.class), any(User.class), eq(testPageable));
    }

    // Helper methods to create mock objects

    private Organization createMockOrganization(String id, String name) {
        Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setUrl("https://github.com/" + name);
        return org;
    }

    private OrganizationRes createMockOrganizationRes(String id, String name) {
        OrganizationRes orgRes = new OrganizationRes();
        orgRes.setId(id);
        orgRes.setName(name);
        orgRes.setUrl("https://github.com/" + name);
        return orgRes;
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

    private RepositoryRes createMockRepositoryRes(String name, String description) {
        RepositoryRes repoRes = new RepositoryRes();
        repoRes.setId("123456");
        repoRes.setName(name);
        repoRes.setDescription(description);
        repoRes.setCloneUrlHttp("https://github.com/test/" + name + ".git");
        repoRes.setCloneUrlSsh("git@github.com:test/" + name + ".git");
        repoRes.setDefaultBranch("main");
        return repoRes;
    }
}

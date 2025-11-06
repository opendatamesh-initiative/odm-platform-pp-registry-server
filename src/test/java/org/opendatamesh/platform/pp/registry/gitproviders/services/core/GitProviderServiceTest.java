package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
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
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderIdentifierRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.CreateRepositoryReqRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.githandler.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private BranchMapper branchMapper;

    @Mock
    private GitProviderFactory gitProviderFactory;

    @Mock
    private GitProvider gitProvider;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GitProviderServiceImpl gitProviderService;

    private Credential testCredential;
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
        )).thenReturn(Optional.of(gitProvider));
        
        when(gitProvider.listOrganizations(testPageable)).thenReturn(mockPage);
        when(organizationMapper.toRes(any(Organization.class))).thenReturn(mockOrgRes1, mockOrgRes2);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);

        // When
        Page<OrganizationRes> result = gitProviderService.listOrganizations(
                providerIdentifier, testCredential, testPageable
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
        )).thenReturn(Optional.of(gitProvider));
        
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        when(gitProvider.listRepositories(any(Organization.class), eq(null), eq(parameters), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(any(Repository.class))).thenReturn(mockRepoRes1, mockRepoRes2);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = new OrganizationRes(organizationId, organizationName, null);

        // Mock the mapper to return domain object
        Organization mockOrg = new Organization();
        mockOrg.setId(organizationId);
        mockOrg.setName(organizationName);
        when(organizationMapper.toEntity(organizationRes)).thenReturn(mockOrg);

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerIdentifier, false, organizationRes, parameters, testCredential, testPageable
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
        verify(gitProvider).listRepositories(any(Organization.class), eq(null), eq(parameters), eq(testPageable));
        verify(repositoryMapper, times(2)).toRes(any(Repository.class));
    }

    @Test
    void whenListRepositoriesWithoutOrganizationThenCreateUserOnlyRequest() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";

        Repository mockRepo = createMockRepository("user-repo", "User Repository");
        Page<Repository> mockPage = new PageImpl<>(Arrays.asList(mockRepo), testPageable, 1);
        RepositoryRes mockRepoRes = createMockRepositoryRes("user-repo", "User Repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(username);
        when(gitProvider.getCurrentUser()).thenReturn(mockUser);
        
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        when(gitProvider.listRepositories(eq(null), any(User.class), eq(parameters), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(mockRepo)).thenReturn(mockRepoRes);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerIdentifier, true, organizationRes, parameters, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockRepoRes);

        verify(gitProvider).getCurrentUser();
        verify(gitProvider).listRepositories(eq(null), any(User.class), eq(parameters), eq(testPageable));
    }

    @Test
    void whenListRepositoriesWithEmptyOrganizationIdThenCreateUserOnlyRequest() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";

        Repository mockRepo = createMockRepository("user-repo", "User Repository");
        Page<Repository> mockPage = new PageImpl<>(Arrays.asList(mockRepo), testPageable, 1);
        RepositoryRes mockRepoRes = createMockRepositoryRes("user-repo", "User Repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(username);
        when(gitProvider.getCurrentUser()).thenReturn(mockUser);
        
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        when(gitProvider.listRepositories(eq(null), any(User.class), eq(parameters), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(mockRepo)).thenReturn(mockRepoRes);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerIdentifier, true, organizationRes, parameters, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockRepoRes);

        verify(gitProvider).getCurrentUser();
        verify(gitProvider).listRepositories(eq(null), any(User.class), eq(parameters), eq(testPageable));
    }

    @Test
    void whenListRepositoriesWithOrganizationNameOnlyThenUseOrganizationName() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
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
        )).thenReturn(Optional.of(gitProvider));
        
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        when(gitProvider.listRepositories(any(Organization.class), eq(null), eq(parameters), eq(testPageable)))
                .thenReturn(mockPage);
        when(repositoryMapper.toRes(mockRepo)).thenReturn(mockRepoRes);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = (organizationId != null && !organizationId.trim().isEmpty()) ? new OrganizationRes(organizationId, organizationName, null) : null;

        // Mock the mapper to return domain object
        if (organizationRes != null) {
            Organization mockOrg = new Organization();
            mockOrg.setId(organizationId);
            mockOrg.setName(organizationName);
            when(organizationMapper.toEntity(organizationRes)).thenReturn(mockOrg);
        }

        // When
        Page<RepositoryRes> result = gitProviderService.listRepositories(
                providerIdentifier, false, organizationRes, parameters, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockRepoRes);

        verify(gitProvider).listRepositories(any(Organization.class), eq(null), eq(parameters), eq(testPageable));
    }

    @Test
    void whenListRepositoriesWithShowUserRepositoriesFalseButNoOrganizationThenThrowBadRequestException() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When & Then
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        assertThatThrownBy(() -> gitProviderService.listRepositories(
                providerIdentifier, false, organizationRes, parameters, testCredential, testPageable
        )).isInstanceOf(BadRequestException.class)
          .hasMessage("Organization information is required when showUserRepositories is false");
    }

    @Test
    void whenCreateRepositoryWithValidParametersThenReturnRepository() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String organizationId = "456";
        String organizationName = "testorg";

        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("test-repo");
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(false);

        Repository mockCreatedRepo = createMockRepository("test-repo", "Test repository");
        RepositoryRes mockRepoRes = createMockRepositoryRes("test-repo", "Test repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        when(gitProvider.createRepository(any(Repository.class))).thenReturn(mockCreatedRepo);
        when(repositoryMapper.toRes(mockCreatedRepo)).thenReturn(mockRepoRes);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = new OrganizationRes(organizationId, organizationName, null);

        // Mock the mapper to return domain object
        Organization mockOrg = new Organization();
        mockOrg.setId(organizationId);
        mockOrg.setName(organizationName);
        when(organizationMapper.toEntity(organizationRes)).thenReturn(mockOrg);

        // When
        RepositoryRes result = gitProviderService.createRepository(
                providerIdentifier, organizationRes, testCredential, createRepositoryReq
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockRepoRes);

        verify(gitProviderFactory).getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        );
        verify(gitProvider).createRepository(any(Repository.class));
        verify(repositoryMapper).toRes(mockCreatedRepo);
    }

    @Test
    void whenCreateRepositoryForUserOnlyThenSetUserAsOwner() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";

        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("user-repo");
        createRepositoryReq.setDescription("User repository");
        createRepositoryReq.setIsPrivate(true);

        Repository mockCreatedRepo = createMockRepository("user-repo", "User repository");
        RepositoryRes mockRepoRes = createMockRepositoryRes("user-repo", "User repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(username);
        when(gitProvider.getCurrentUser()).thenReturn(mockUser);
        
        when(gitProvider.createRepository(any(Repository.class))).thenReturn(mockCreatedRepo);
        when(repositoryMapper.toRes(mockCreatedRepo)).thenReturn(mockRepoRes);

        // Create test DTOs - no organization
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When
        RepositoryRes result = gitProviderService.createRepository(
                providerIdentifier, organizationRes, testCredential, createRepositoryReq
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockRepoRes);

        verify(gitProvider).getCurrentUser();
        verify(gitProvider).createRepository(any(Repository.class));
        verify(repositoryMapper).toRes(mockCreatedRepo);
    }

    @Test
    void whenCreateRepositoryWithEmptyOrganizationIdThenSetUserAsOwner() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";

        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("user-repo");
        createRepositoryReq.setDescription("User repository");
        createRepositoryReq.setIsPrivate(false);

        Repository mockCreatedRepo = createMockRepository("user-repo", "User repository");
        RepositoryRes mockRepoRes = createMockRepositoryRes("user-repo", "User repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(username);
        when(gitProvider.getCurrentUser()).thenReturn(mockUser);
        
        when(gitProvider.createRepository(any(Repository.class))).thenReturn(mockCreatedRepo);
        when(repositoryMapper.toRes(mockCreatedRepo)).thenReturn(mockRepoRes);

        // Create test DTOs - empty organization ID should be treated as null
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When
        RepositoryRes result = gitProviderService.createRepository(
                providerIdentifier, organizationRes, testCredential, createRepositoryReq
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockRepoRes);

        verify(gitProvider).getCurrentUser();
        verify(gitProvider).createRepository(any(Repository.class));
        verify(repositoryMapper).toRes(mockCreatedRepo);
    }

    @Test
    void whenCreateRepositoryWithEmptyNameThenThrowBadRequestException() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";

        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName(""); // Empty name
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(false);

        // Mock the gitProviderFactory to return a valid provider
        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When & Then
        assertThatThrownBy(() -> gitProviderService.createRepository(
                providerIdentifier, organizationRes, testCredential, createRepositoryReq
        )).isInstanceOf(BadRequestException.class)
          .hasMessage("Repository name is required and cannot be empty");
    }

    @Test
    void whenCreateRepositoryWithNullNameThenThrowBadRequestException() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";

        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName(null); // Null name
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(false);

        // Mock the gitProviderFactory to return a valid provider
        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When & Then
        assertThatThrownBy(() -> gitProviderService.createRepository(
                providerIdentifier, organizationRes, testCredential, createRepositoryReq
        )).isInstanceOf(BadRequestException.class)
          .hasMessage("Repository name is required and cannot be empty");
    }


    @Test
    void whenCreateRepositoryWithNullIsPrivateThenThrowBadRequestException() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";

        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("test-repo");
        createRepositoryReq.setDescription("Test repository");
        createRepositoryReq.setIsPrivate(null); // Null isPrivate

        // Mock the gitProviderFactory to return a valid provider
        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When & Then
        assertThatThrownBy(() -> gitProviderService.createRepository(
                providerIdentifier, organizationRes, testCredential, createRepositoryReq
        )).isInstanceOf(BadRequestException.class)
          .hasMessage("Repository visibility (isPrivate) is required and cannot be null");
    }

    @Test
    void whenCreateRepositoryWithNullDescriptionThenSucceed() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String userId = "123";
        String username = "testuser";

        CreateRepositoryReqRes createRepositoryReq = new CreateRepositoryReqRes();
        createRepositoryReq.setName("test-repo");
        createRepositoryReq.setDescription(null); // Null description should be allowed
        createRepositoryReq.setIsPrivate(false);

        Repository mockCreatedRepo = createMockRepository("test-repo", null);
        RepositoryRes mockRepoRes = createMockRepositoryRes("test-repo", null);

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername(username);
        when(gitProvider.getCurrentUser()).thenReturn(mockUser);
        
        when(gitProvider.createRepository(any(Repository.class))).thenReturn(mockCreatedRepo);
        when(repositoryMapper.toRes(mockCreatedRepo)).thenReturn(mockRepoRes);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        OrganizationRes organizationRes = null;

        // When
        RepositoryRes result = gitProviderService.createRepository(
                providerIdentifier, organizationRes, testCredential, createRepositoryReq
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockRepoRes);

        verify(gitProvider).getCurrentUser();
        verify(gitProvider).createRepository(any(Repository.class));
        verify(repositoryMapper).toRes(mockCreatedRepo);
    }

    @Test
    void whenListBranchesWithValidRepositoryIdThenReturnBranches() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String repositoryId = "123456";
        String ownerId = "123456";

        Branch mockBranch1 = createMockBranch("main", "abc123", true);
        Branch mockBranch2 = createMockBranch("develop", "def456", false);
        List<Branch> mockBranches = Arrays.asList(mockBranch1, mockBranch2);
        Page<Branch> mockPage = new PageImpl<>(mockBranches, testPageable, 2);

        BranchRes mockBranchRes1 = createMockBranchRes("main", "abc123", true);
        BranchRes mockBranchRes2 = createMockBranchRes("develop", "def456", false);

        Repository mockRepository = createMockRepository("test-repo", "Test Repository");

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        when(gitProvider.getRepository(repositoryId, ownerId)).thenReturn(Optional.of(mockRepository));
        when(gitProvider.listBranches(any(Repository.class), eq(testPageable))).thenReturn(mockPage);
        when(branchMapper.toRes(any(Branch.class))).thenReturn(mockBranchRes1, mockBranchRes2);

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);

        // When
        Page<BranchRes> result = gitProviderService.listBranches(
                providerIdentifier, repositoryId, ownerId, testCredential, testPageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockBranchRes1, mockBranchRes2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(gitProviderFactory).getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        );
        verify(gitProvider).getRepository(repositoryId, ownerId);
        verify(gitProvider).listBranches(any(Repository.class), eq(testPageable));
        verify(branchMapper, times(2)).toRes(any(Branch.class));
    }

    @Test
    void whenListBranchesWithNonExistentRepositoryIdThenThrowBadRequestException() {
        // Given
        String providerType = "GITHUB";
        String providerBaseUrl = "https://api.github.com";
        String repositoryId = "non-existent-id";
        String ownerId = "123456";

        when(gitProviderFactory.getProvider(
                any(DataProductRepoProviderType.class),
                any(String.class),
                any(RestTemplate.class),
                any(PatCredential.class)
        )).thenReturn(Optional.of(gitProvider));
        
        when(gitProvider.getRepository(repositoryId, ownerId)).thenReturn(Optional.empty());

        // Create test DTOs
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);

        // When & Then
        assertThatThrownBy(() -> gitProviderService.listBranches(
                providerIdentifier, repositoryId, ownerId, testCredential, testPageable
        )).isInstanceOf(BadRequestException.class)
          .hasMessage("Repository not found with ID: " + repositoryId);

        verify(gitProvider).getRepository(repositoryId, ownerId);
        verify(gitProvider, never()).listBranches(any(Repository.class), any(Pageable.class));
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

    private Branch createMockBranch(String name, String commitHash, boolean isDefault) {
        Branch branch = new Branch();
        branch.setName(name);
        branch.setCommitHash(commitHash);
        branch.setDefault(isDefault);
        branch.setProtected(false);
        return branch;
    }

    private BranchRes createMockBranchRes(String name, String commitHash, boolean isDefault) {
        return new BranchRes(name, commitHash, isDefault, false);
    }
}

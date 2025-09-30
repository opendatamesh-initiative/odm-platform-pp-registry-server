package org.opendatamesh.platform.pp.registry.dataproduct.services.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductsUtilsServiceImpl;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.Branch;
import org.opendatamesh.platform.pp.registry.githandler.model.Commit;
import org.opendatamesh.platform.pp.registry.githandler.model.Tag;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataProductUtilsServiceTest {

    @Mock
    private DataProductsService service;

    @Mock
    private GitProviderFactory gitProviderFactory;

    @Mock
    private GitProvider gitProvider;

    @Mock
    private CommitMapper commitMapper;

    @Mock
    private BranchMapper branchMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private DataProductsUtilsServiceImpl dataProductsUtilsService;

    private static final String TEST_UUID = "test-uuid-123";
    private static final String TEST_USER_ID = "123";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_ORG_ID = "456";
    private static final String TEST_ORG_NAME = "testorg";
    private static final String TEST_PAT_TOKEN = "test-pat-token";
    private static final String TEST_PAT_USERNAME = "test-pat-user";

    private DataProduct testDataProduct;
    private DataProductRepo testDataProductRepo;
    private Credential testCredential;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        // Setup test data product
        testDataProduct = new DataProduct();
        testDataProduct.setUuid(TEST_UUID);

        // Setup test data product repo
        testDataProductRepo = new DataProductRepo();
        testDataProductRepo.setProviderType(DataProductRepoProviderType.GITHUB);
        testDataProductRepo.setProviderBaseUrl("https://api.github.com");
        testDataProductRepo.setName("test-repo");
        testDataProductRepo.setRemoteUrlHttp("https://github.com/test/test-repo.git");
        testDataProductRepo.setRemoteUrlSsh("git@github.com:test/test-repo.git");
        testDataProductRepo.setDefaultBranch("main");
        testDataProduct.setDataProductRepo(testDataProductRepo);

        // Setup test credential
        testCredential = new PatCredential(TEST_PAT_USERNAME, TEST_PAT_TOKEN);

        // Setup test pageable
        testPageable = PageRequest.of(0, 10);
    }

    @Test
    void whenListCommitsWithValidDataProductThenReturnCommits() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.getProvider(any(), any(), any(), any())).thenReturn(Optional.of(gitProvider));

        Commit mockCommit1 = createMockCommit("abc123", "Initial commit");
        Commit mockCommit2 = createMockCommit("def456", "Add feature");
        List<Commit> mockCommits = Arrays.asList(mockCommit1, mockCommit2);
        Page<Commit> mockPage = new PageImpl<>(mockCommits, testPageable, 2);

        when(gitProvider.listCommits(any(), any(), any(), any())).thenReturn(mockPage);

        CommitRes mockCommitRes1 = createMockCommitRes("abc123", "Initial commit");
        CommitRes mockCommitRes2 = createMockCommitRes("def456", "Add feature");
        when(commitMapper.toRes(mockCommit1)).thenReturn(mockCommitRes1);
        when(commitMapper.toRes(mockCommit2)).thenReturn(mockCommitRes2);

        // Create test DTOs
        UserRes userRes = new UserRes(TEST_USER_ID, TEST_USERNAME);
        OrganizationRes organizationRes = new OrganizationRes(TEST_ORG_ID, TEST_ORG_NAME, null);

        // When
        Page<CommitRes> result = dataProductsUtilsService.listCommits(
                TEST_UUID, userRes, organizationRes, testCredential, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockCommitRes1, mockCommitRes2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).getProvider(any(), any(), any(), any());
        verify(gitProvider).listCommits(any(), any(), any(), any());
    }

    @Test
    void whenListCommitsWithDataProductWithoutRepoThenThrowBadRequestException() {
        // Given
        testDataProduct.setDataProductRepo(null);
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);

        // Create test DTOs
        UserRes userRes = new UserRes(TEST_USER_ID, TEST_USERNAME);
        OrganizationRes organizationRes = new OrganizationRes(TEST_ORG_ID, TEST_ORG_NAME, null);

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, userRes, organizationRes, testCredential, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data product does not have an associated repository");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListBranchesWithValidDataProductThenReturnBranches() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.getProvider(any(), any(), any(), any())).thenReturn(Optional.of(gitProvider));

        Branch mockBranch1 = createMockBranch("main", "abc123", true);
        Branch mockBranch2 = createMockBranch("develop", "def456", false);
        List<Branch> mockBranches = Arrays.asList(mockBranch1, mockBranch2);
        Page<Branch> mockPage = new PageImpl<>(mockBranches, testPageable, 2);

        when(gitProvider.listBranches(any(), any(), any(), any())).thenReturn(mockPage);

        BranchRes mockBranchRes1 = createMockBranchRes("main", "abc123", true);
        BranchRes mockBranchRes2 = createMockBranchRes("develop", "def456", false);
        when(branchMapper.toRes(mockBranch1)).thenReturn(mockBranchRes1);
        when(branchMapper.toRes(mockBranch2)).thenReturn(mockBranchRes2);

        // Create test DTOs
        UserRes userRes = new UserRes(TEST_USER_ID, TEST_USERNAME);
        OrganizationRes organizationRes = new OrganizationRes(TEST_ORG_ID, TEST_ORG_NAME, null);

        // When
        Page<BranchRes> result = dataProductsUtilsService.listBranches(
                TEST_UUID, userRes, organizationRes, testCredential, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockBranchRes1, mockBranchRes2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).getProvider(any(), any(), any(), any());
        verify(gitProvider).listBranches(any(), any(), any(), any());
    }

    @Test
    void whenListBranchesWithDataProductWithoutRepoThenThrowBadRequestException() {
        // Given
        testDataProduct.setDataProductRepo(null);
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);

        // Create test DTOs
        UserRes userRes = new UserRes(TEST_USER_ID, TEST_USERNAME);
        OrganizationRes organizationRes = new OrganizationRes(TEST_ORG_ID, TEST_ORG_NAME, null);

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listBranches(
                TEST_UUID, userRes, organizationRes, testCredential, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data product does not have an associated repository");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListTagsWithValidDataProductThenReturnTags() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.getProvider(any(), any(), any(), any())).thenReturn(Optional.of(gitProvider));

        Tag mockTag1 = createMockTag("v1.0.0", "abc123");
        Tag mockTag2 = createMockTag("v1.1.0", "def456");
        List<Tag> mockTags = Arrays.asList(mockTag1, mockTag2);
        Page<Tag> mockPage = new PageImpl<>(mockTags, testPageable, 2);

        when(gitProvider.listTags(any(), any(), any(), any())).thenReturn(mockPage);

        TagRes mockTagRes1 = createMockTagRes("v1.0.0", "abc123");
        TagRes mockTagRes2 = createMockTagRes("v1.1.0", "def456");
        when(tagMapper.toRes(mockTag1)).thenReturn(mockTagRes1);
        when(tagMapper.toRes(mockTag2)).thenReturn(mockTagRes2);

        // Create test DTOs
        UserRes userRes = new UserRes(TEST_USER_ID, TEST_USERNAME);
        OrganizationRes organizationRes = new OrganizationRes(TEST_ORG_ID, TEST_ORG_NAME, null);

        // When
        Page<TagRes> result = dataProductsUtilsService.listTags(
                TEST_UUID, userRes, organizationRes, testCredential, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockTagRes1, mockTagRes2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).getProvider(any(), any(), any(), any());
        verify(gitProvider).listTags(any(), any(), any(), any());
    }

    @Test
    void whenListTagsWithDataProductWithoutRepoThenThrowBadRequestException() {
        // Given
        testDataProduct.setDataProductRepo(null);
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);

        // Create test DTOs
        UserRes userRes = new UserRes(TEST_USER_ID, TEST_USERNAME);
        OrganizationRes organizationRes = new OrganizationRes(TEST_ORG_ID, TEST_ORG_NAME, null);

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listTags(
                TEST_UUID, userRes, organizationRes, testCredential, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data product does not have an associated repository");

        verify(service).findOne(TEST_UUID);
    }

    // Helper methods to create mock objects
    private Commit createMockCommit(String hash, String message) {
        Commit commit = new Commit();
        commit.setHash(hash);
        commit.setMessage(message);
        commit.setAuthorEmail("author@example.com");
        commit.setCommitDate(new java.util.Date());
        return commit;
    }

    private CommitRes createMockCommitRes(String hash, String message) {
        return new CommitRes(hash, message, "author@example.com", new java.util.Date());
    }

    private Branch createMockBranch(String name, String latestCommitHash, boolean isDefault) {
        Branch branch = new Branch();
        branch.setName(name);
        branch.setCommitHash(latestCommitHash);
        branch.setDefault(isDefault);
        branch.setProtected(false);
        return branch;
    }

    private BranchRes createMockBranchRes(String name, String latestCommitHash, boolean isDefault) {
        return new BranchRes(name, latestCommitHash, isDefault, false);
    }

    private Tag createMockTag(String name, String commitHash) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCommitHash(commitHash);
        return tag;
    }

    private TagRes createMockTagRes(String name, String commitHash) {
        return new TagRes(name, commitHash);
    }
}

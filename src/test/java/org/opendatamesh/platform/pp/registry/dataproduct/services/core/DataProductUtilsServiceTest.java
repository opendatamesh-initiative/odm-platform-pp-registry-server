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
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitSearchOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;

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


    @InjectMocks
    private DataProductsUtilsServiceImpl dataProductsUtilsService;

    private static final String TEST_UUID = "test-uuid-123";

    private DataProduct testDataProduct;
    private DataProductRepo testDataProductRepo;
    private HttpHeaders testHeaders;
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

        // Setup test headers
        testHeaders = new HttpHeaders();
        testHeaders.set("x-odm-gpauth-type", "PAT");
        testHeaders.set("x-odm-gpauth-param-token", "test-pat-token");

        // Setup test pageable
        testPageable = PageRequest.of(0, 10);
    }

    @Test
    void whenListCommitsWithValidDataProductThenReturnCommits() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.buildGitProvider(any(), any())).thenReturn(gitProvider);

        Commit mockCommit1 = createMockCommit("abc123", "Initial commit");
        Commit mockCommit2 = createMockCommit("def456", "Add feature");
        List<Commit> mockCommits = Arrays.asList(mockCommit1, mockCommit2);
        Page<Commit> mockPage = new PageImpl<>(mockCommits, testPageable, 2);

        when(gitProvider.listCommits(any(), any(), any())).thenReturn(mockPage);

        CommitRes mockCommitRes1 = createMockCommitRes("abc123", "Initial commit");
        CommitRes mockCommitRes2 = createMockCommitRes("def456", "Add feature");
        CommitSearchOptions testSearchOptions = new CommitSearchOptions();
        when(commitMapper.toRes(mockCommit1)).thenReturn(mockCommitRes1);
        when(commitMapper.toRes(mockCommit2)).thenReturn(mockCommitRes2);

        // When
        Page<CommitRes> result = dataProductsUtilsService.listCommits(
                TEST_UUID, testHeaders, testSearchOptions, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockCommitRes1, mockCommitRes2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).buildGitProvider(any(), any());
        verify(gitProvider).listCommits(any(), any(), any());
    }

    @Test
    void whenListCommitsWithDataProductWithoutRepoThenThrowBadRequestException() {
        // Given
        testDataProduct.setDataProductRepo(null);
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions testSearchOptions = new CommitSearchOptions();

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testHeaders, testSearchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data product does not have an associated repository");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListBranchesWithValidDataProductThenReturnBranches() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.buildGitProvider(any(), any())).thenReturn(gitProvider);

        Branch mockBranch1 = createMockBranch("main", "abc123", true);
        Branch mockBranch2 = createMockBranch("develop", "def456", false);
        List<Branch> mockBranches = Arrays.asList(mockBranch1, mockBranch2);
        Page<Branch> mockPage = new PageImpl<>(mockBranches, testPageable, 2);

        when(gitProvider.listBranches(any(), any())).thenReturn(mockPage);

        BranchRes mockBranchRes1 = createMockBranchRes("main", "abc123", true);
        BranchRes mockBranchRes2 = createMockBranchRes("develop", "def456", false);
        when(branchMapper.toRes(mockBranch1)).thenReturn(mockBranchRes1);
        when(branchMapper.toRes(mockBranch2)).thenReturn(mockBranchRes2);

        // When
        Page<BranchRes> result = dataProductsUtilsService.listBranches(
                TEST_UUID, testHeaders, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockBranchRes1, mockBranchRes2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).buildGitProvider(any(), any());
        verify(gitProvider).listBranches(any(), any());
    }

    @Test
    void whenListBranchesWithDataProductWithoutRepoThenThrowBadRequestException() {
        // Given
        testDataProduct.setDataProductRepo(null);
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listBranches(
                TEST_UUID, testHeaders, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data product does not have an associated repository");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListTagsWithValidDataProductThenReturnTags() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.buildGitProvider(any(), any())).thenReturn(gitProvider);

        Tag mockTag1 = createMockTag("v1.0.0", "abc123");
        Tag mockTag2 = createMockTag("v1.1.0", "def456");
        List<Tag> mockTags = Arrays.asList(mockTag1, mockTag2);
        Page<Tag> mockPage = new PageImpl<>(mockTags, testPageable, 2);

        when(gitProvider.listTags(any(), any())).thenReturn(mockPage);

        TagRes mockTagRes1 = createMockTagRes("v1.0.0", "abc123");
        TagRes mockTagRes2 = createMockTagRes("v1.1.0", "def456");
        when(tagMapper.toRes(mockTag1)).thenReturn(mockTagRes1);
        when(tagMapper.toRes(mockTag2)).thenReturn(mockTagRes2);

        // When
        Page<TagRes> result = dataProductsUtilsService.listTags(
                TEST_UUID, testHeaders, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockTagRes1, mockTagRes2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).buildGitProvider(any(), any());
        verify(gitProvider).listTags(any(), any());
    }

    @Test
    void whenListTagsWithDataProductWithoutRepoThenThrowBadRequestException() {
        // Given
        testDataProduct.setDataProductRepo(null);
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listTags(
                TEST_UUID, testHeaders, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Data product does not have an associated repository");

        verify(service).findOne(TEST_UUID);
    }

    // ===== CommitSearchOptions Validation Tests =====

    @Test
    void whenListCommitsWithOnlyFromTagNameThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromTagName("v1.0.0");
        // toTagName is not set

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromTagName and toTagName must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithOnlyToTagNameThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setToTagName("v2.0.0");
        // fromTagName is not set

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromTagName and toTagName must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithOnlyFromCommitHashThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromCommitHash("abc123");
        // toCommitHash is not set

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromCommitHash and toCommitHash must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithOnlyToCommitHashThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setToCommitHash("def456");
        // fromCommitHash is not set

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromCommitHash and toCommitHash must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithOnlyFromBranchNameThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromBranchName("main");
        // toBranchName is not set

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromBranchName and toBranchName must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithOnlyToBranchNameThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setToBranchName("develop");
        // fromBranchName is not set

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromBranchName and toBranchName must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithTagsAndCommitHashesThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromTagName("v1.0.0");
        searchOptions.setToTagName("v2.0.0");
        searchOptions.setFromCommitHash("abc123");
        searchOptions.setToCommitHash("def456");

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only one type of comparison can be used at a time (tags, commit hashes, or branches)");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithTagsAndBranchesThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromTagName("v1.0.0");
        searchOptions.setToTagName("v2.0.0");
        searchOptions.setFromBranchName("main");
        searchOptions.setToBranchName("develop");

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only one type of comparison can be used at a time (tags, commit hashes, or branches)");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithCommitHashesAndBranchesThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromCommitHash("abc123");
        searchOptions.setToCommitHash("def456");
        searchOptions.setFromBranchName("main");
        searchOptions.setToBranchName("develop");

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only one type of comparison can be used at a time (tags, commit hashes, or branches)");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithAllThreeTypesThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromTagName("v1.0.0");
        searchOptions.setToTagName("v2.0.0");
        searchOptions.setFromCommitHash("abc123");
        searchOptions.setToCommitHash("def456");
        searchOptions.setFromBranchName("main");
        searchOptions.setToBranchName("develop");

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only one type of comparison can be used at a time (tags, commit hashes, or branches)");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithEmptyFromTagNameThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromTagName("");
        searchOptions.setToTagName("v2.0.0");

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromTagName and toTagName must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithEmptyToTagNameThenThrowBadRequestException() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromTagName("v1.0.0");
        searchOptions.setToTagName("");

        // When & Then
        assertThatThrownBy(() -> dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Both fromTagName and toTagName must be defined together");

        verify(service).findOne(TEST_UUID);
    }

    @Test
    void whenListCommitsWithValidTagPairThenReturnCommits() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.getProvider(any(), any(), any(), any())).thenReturn(Optional.of(gitProvider));

        Commit mockCommit1 = createMockCommit("abc123", "Initial commit");
        Commit mockCommit2 = createMockCommit("def456", "Add feature");
        List<Commit> mockCommits = Arrays.asList(mockCommit1, mockCommit2);
        Page<Commit> mockPage = new PageImpl<>(mockCommits, testPageable, 2);

        when(gitProvider.listCommits(any(), any(), any())).thenReturn(mockPage);

        CommitRes mockCommitRes1 = createMockCommitRes("abc123", "Initial commit");
        CommitRes mockCommitRes2 = createMockCommitRes("def456", "Add feature");
        when(commitMapper.toRes(mockCommit1)).thenReturn(mockCommitRes1);
        when(commitMapper.toRes(mockCommit2)).thenReturn(mockCommitRes2);

        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromTagName("v1.0.0");
        searchOptions.setToTagName("v2.0.0");

        // When
        Page<CommitRes> result = dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).getProvider(any(), any(), any(), any());
        verify(gitProvider).listCommits(any(), any(), any());
    }

    @Test
    void whenListCommitsWithValidCommitHashPairThenReturnCommits() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.getProvider(any(), any(), any(), any())).thenReturn(Optional.of(gitProvider));

        Commit mockCommit1 = createMockCommit("abc123", "Initial commit");
        Commit mockCommit2 = createMockCommit("def456", "Add feature");
        List<Commit> mockCommits = Arrays.asList(mockCommit1, mockCommit2);
        Page<Commit> mockPage = new PageImpl<>(mockCommits, testPageable, 2);

        when(gitProvider.listCommits(any(), any(), any())).thenReturn(mockPage);

        CommitRes mockCommitRes1 = createMockCommitRes("abc123", "Initial commit");
        CommitRes mockCommitRes2 = createMockCommitRes("def456", "Add feature");
        when(commitMapper.toRes(mockCommit1)).thenReturn(mockCommitRes1);
        when(commitMapper.toRes(mockCommit2)).thenReturn(mockCommitRes2);

        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromCommitHash("abc123");
        searchOptions.setToCommitHash("def456");

        // When
        Page<CommitRes> result = dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).getProvider(any(), any(), any(), any());
        verify(gitProvider).listCommits(any(), any(), any());
    }

    @Test
    void whenListCommitsWithValidBranchPairThenReturnCommits() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.getProvider(any(), any(), any(), any())).thenReturn(Optional.of(gitProvider));

        Commit mockCommit1 = createMockCommit("abc123", "Initial commit");
        Commit mockCommit2 = createMockCommit("def456", "Add feature");
        List<Commit> mockCommits = Arrays.asList(mockCommit1, mockCommit2);
        Page<Commit> mockPage = new PageImpl<>(mockCommits, testPageable, 2);

        when(gitProvider.listCommits(any(), any(), any())).thenReturn(mockPage);

        CommitRes mockCommitRes1 = createMockCommitRes("abc123", "Initial commit");
        CommitRes mockCommitRes2 = createMockCommitRes("def456", "Add feature");
        when(commitMapper.toRes(mockCommit1)).thenReturn(mockCommitRes1);
        when(commitMapper.toRes(mockCommit2)).thenReturn(mockCommitRes2);

        CommitSearchOptions searchOptions = new CommitSearchOptions();
        searchOptions.setFromBranchName("main");
        searchOptions.setToBranchName("develop");

        // When
        Page<CommitRes> result = dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, searchOptions, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).getProvider(any(), any(), any(), any());
        verify(gitProvider).listCommits(any(), any(), any());
    }

    @Test
    void whenListCommitsWithNullSearchOptionsThenReturnCommits() {
        // Given
        when(service.findOne(TEST_UUID)).thenReturn(testDataProduct);
        when(gitProviderFactory.getProvider(any(), any(), any(), any())).thenReturn(Optional.of(gitProvider));

        Commit mockCommit1 = createMockCommit("abc123", "Initial commit");
        Commit mockCommit2 = createMockCommit("def456", "Add feature");
        List<Commit> mockCommits = Arrays.asList(mockCommit1, mockCommit2);
        Page<Commit> mockPage = new PageImpl<>(mockCommits, testPageable, 2);

        when(gitProvider.listCommits(any(), any(), any())).thenReturn(mockPage);

        CommitRes mockCommitRes1 = createMockCommitRes("abc123", "Initial commit");
        CommitRes mockCommitRes2 = createMockCommitRes("def456", "Add feature");
        when(commitMapper.toRes(mockCommit1)).thenReturn(mockCommitRes1);
        when(commitMapper.toRes(mockCommit2)).thenReturn(mockCommitRes2);

        // When
        Page<CommitRes> result = dataProductsUtilsService.listCommits(
                TEST_UUID, testCredential, null, testPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(service).findOne(TEST_UUID);
        verify(gitProviderFactory).getProvider(any(), any(), any(), any());
        verify(gitProvider).listCommits(any(), any(), any());
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

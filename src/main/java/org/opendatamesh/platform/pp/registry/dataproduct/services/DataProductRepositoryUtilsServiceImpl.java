package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductAdditionalRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoOwnerType;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.*;
import org.opendatamesh.platform.git.exceptions.GitOperationException;
import org.opendatamesh.platform.git.model.*;
import org.opendatamesh.platform.git.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.git.provider.GitProviderFactory;
import org.opendatamesh.platform.git.provider.GitProviderIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class DataProductRepositoryUtilsServiceImpl implements DataProductRepositoryUtilsService {

    private final DataProductsService service;
    private final CommitMapper commitMapper;
    private final BranchMapper branchMapper;
    private final TagMapper tagMapper;
    private final GitProviderFactory gitProviderFactory;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DataProductRepositoryUtilsServiceImpl(DataProductsService service,
                                                 CommitMapper commitMapper, BranchMapper branchMapper, TagMapper tagMapper,
                                                 GitProviderFactory gitProviderFactory) {
        this.service = service;
        this.commitMapper = commitMapper;
        this.branchMapper = branchMapper;
        this.tagMapper = tagMapper;
        this.gitProviderFactory = gitProviderFactory;
    }

    @Override
    public Page<CommitRes> listCommits(String dataProductUuid, HttpHeaders headers, CommitSearchOptions searchOptions, Pageable pageable) {
        DataProductRepo dataProductRepo = Optional.ofNullable(service.findOne(dataProductUuid).getDataProductRepo())
                .orElseThrow(() -> new BadRequestException("Data product does not have an associated repository"));

        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers);

        Repository repository = buildRepoObject(dataProductRepo);

        CommitListFilter commitListFilter = buildCommitListFilterFromOptions(searchOptions, dataProductRepo.getDefaultBranch());

        return gitProvider.listCommits(repository, commitListFilter, pageable)
                .map(commitMapper::toRes);
    }

    @Override
    public Page<BranchRes> listBranches(String dataProductUuid, HttpHeaders headers, Pageable pageable) {
        DataProductRepo dataProductRepo = Optional.ofNullable(service.findOne(dataProductUuid).getDataProductRepo())
                .orElseThrow(() -> new BadRequestException("Data product does not have an associated repository"));

        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers);

        Repository repository = buildRepoObject(dataProductRepo);
        return gitProvider.listBranches(repository, pageable)
                .map(branchMapper::toRes);
    }

    @Override
    public Page<TagRes> listTags(String dataProductUuid, HttpHeaders headers, Pageable pageable) {
        DataProductRepo dataProductRepo = Optional.ofNullable(service.findOne(dataProductUuid).getDataProductRepo())
                .orElseThrow(() -> new BadRequestException("Data product does not have an associated repository"));

        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers);
        Repository repository = buildRepoObject(dataProductRepo);

        return gitProvider.listTags(repository, pageable)
                .map(tagMapper::toRes);
    }

    @Override
    public TagRes tagAllDataProductRepositories(String dataProductUuid, TagRes tagRes, HttpHeaders headers) {
        logger.info("Adding tag for data product {}: tagName={}", dataProductUuid, tagRes.getName());
        if (!StringUtils.hasText(tagRes.getName())) {
            throw new BadRequestException("Missing tag name");
        }
        DataProduct dataProduct = service.findOne(dataProductUuid);
        DataProductRepo dataProductRepo = Optional.ofNullable(dataProduct.getDataProductRepo())
                .orElseThrow(() -> new BadRequestException("Data product does not have an associated repository"));

        GitProvider rootProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers);
        Repository rootGitRepo = rootProvider.getRepository(dataProductRepo.getExternalIdentifier(), dataProductRepo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + dataProductRepo.getUuid()));

        List<DataProductAdditionalRepo> additionalRepos = dataProduct.getAdditionalDataProductRepos();
        if (remoteHasTag(rootProvider, rootGitRepo, tagRes.getName())) {
            verifyAdditionalRepositoriesHaveTag(additionalRepos, tagRes.getName(), headers);
            logger.info("Tag {} already present on root and additional repositories for data product {}",
                    tagRes.getName(), dataProductUuid);
            return tagRes;
        }

        assertAdditionalRepositoriesReadyForNewTag(additionalRepos, tagRes, headers);
        tagRootRepository(dataProductUuid, dataProductRepo, tagRes, headers);
        tagAdditionalRepositories(dataProductUuid, additionalRepos, tagRes, headers);

        logger.info("Tag {} added successfully for data product {}", tagRes.getName(), dataProductUuid);
        return tagRes;
    }

    private void tagRootRepository(String dataProductUuid, DataProductRepo dataProductRepo, TagRes tagRes, HttpHeaders headers) {
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers);

        String branchName = StringUtils.hasText(tagRes.getBranchName()) ? tagRes.getBranchName()
                : dataProductRepo.getDefaultBranch();

        Repository gitRepo = provider.getRepository(dataProductRepo.getExternalIdentifier(), dataProductRepo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + dataProductRepo.getUuid()));

        RepositoryPointer repositoryPointer = buildRepositoryPointer(new GitReference(null, branchName, null));
        try {
            provider.gitOperation().readRepository(gitRepo, repositoryPointer, repository -> {
                String targetSha = retrieveTagTargetCommit(tagRes, repository, provider, dataProductRepo);
                applyTagAndPush(provider, repository, tagRes, targetSha);
            });
        } catch (GitOperationException e) {
            logger.warn("Failed to create tag {} on root repository for data product {}: {}",
                    tagRes.getName(), dataProductUuid, e.getMessage(), e);
            throw new BadRequestException("Failed to create tag: " + e.getMessage());
        }
        logger.info("Tag {} added successfully on root repository for data product {}", tagRes.getName(), dataProductUuid);
    }

    private void tagAdditionalRepositories(
            String dataProductUuid,
            List<DataProductAdditionalRepo> additionalRepos,
            TagRes tagRes,
            HttpHeaders headers
    ) {
        if (additionalRepos == null || additionalRepos.isEmpty()) {
            return;
        }
        for (DataProductAdditionalRepo additionalRepo : additionalRepos) {
            tagAdditionalRepository(dataProductUuid, additionalRepo, tagRes, headers);
        }
    }

    private void tagAdditionalRepository(
            String dataProductUuid,
            DataProductAdditionalRepo additionalRepo,
            TagRes tagRes,
            HttpHeaders headers
    ) {
        String identitySuffix = additionalRepoIdentitySuffix(additionalRepo);
        if (!StringUtils.hasText(additionalRepo.getExternalIdentifier())) {
            throw new BadRequestException("Additional repository is missing externalIdentifier" + identitySuffix);
        }
        if (!StringUtils.hasText(additionalRepo.getOwnerId())) {
            throw new BadRequestException("Additional repository is missing ownerId" + identitySuffix);
        }

        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(additionalRepo.getProviderType().name(), additionalRepo.getProviderBaseUrl()),
                headers);

        Repository gitRepo = provider.getRepository(additionalRepo.getExternalIdentifier(), additionalRepo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + additionalRepo.getUuid()));

        String branchName = StringUtils.hasText(tagRes.getBranchName())
                ? tagRes.getBranchName()
                : additionalRepo.getDefaultBranch();
        if (!StringUtils.hasText(branchName)) {
            throw new BadRequestException("Missing branch name for additional repository" + identitySuffix);
        }

        RepositoryPointer repositoryPointer = buildRepositoryPointer(new GitReference(null, branchName, null));
        try {
            provider.gitOperation().readRepository(gitRepo, repositoryPointer, repository -> {
                String targetSha = provider.gitOperation().getHeadSha(repository, branchName);
                applyTagAndPush(provider, repository, tagRes, targetSha);
            });
        } catch (GitOperationException e) {
            logger.warn("Failed to create tag {} on additional repository{} for data product {}: {}",
                    tagRes.getName(), identitySuffix, dataProductUuid, e.getMessage(), e);
            throw new BadRequestException("Failed to create tag: " + e.getMessage());
        }
        logger.info("Tag {} added successfully on additional repository{} for data product {}",
                tagRes.getName(), identitySuffix, dataProductUuid);
    }

    private void verifyAdditionalRepositoriesHaveTag(
            List<DataProductAdditionalRepo> additionalRepos,
            String tagName,
            HttpHeaders headers
    ) {
        if (additionalRepos == null || additionalRepos.isEmpty()) {
            return;
        }
        for (DataProductAdditionalRepo additionalRepo : additionalRepos) {
            AdditionalGitRemote remote = resolveAdditionalGitRemote(additionalRepo, headers);
            if (!remoteHasTag(remote.provider(), remote.gitRepo(), tagName)) {
                throw new BadRequestException(
                        "Tag '" + tagName + "' exists on the root repository but is missing on additional repository '"
                                + additionalRepoDisplayName(additionalRepo)
                                + "'. Create tag '" + tagName + "' on that additional repository, then retry publishing."
                );
            }
        }
    }

    private void assertAdditionalRepositoriesReadyForNewTag(
            List<DataProductAdditionalRepo> additionalRepos,
            TagRes tagRes,
            HttpHeaders headers
    ) {
        if (additionalRepos == null || additionalRepos.isEmpty()) {
            return;
        }
        for (DataProductAdditionalRepo additionalRepo : additionalRepos) {
            AdditionalGitRemote remote = resolveAdditionalGitRemote(additionalRepo, headers);
            String tagName = tagRes.getName();
            String displayName = additionalRepoDisplayName(additionalRepo);
            if (remoteHasTag(remote.provider(), remote.gitRepo(), tagName)) {
                throw new BadRequestException(
                        "Cannot create tag '" + tagName + "': it already exists on additional repository '"
                                + displayName
                                + "'. Choose a different tag name, or delete that tag on the additional repository, then retry publishing."
                );
            }
            if (StringUtils.hasText(tagRes.getBranchName())
                    && !tagRes.getBranchName().equals(additionalRepo.getDefaultBranch())
                    && !remoteHasBranch(remote.provider(), remote.gitRepo(), tagRes.getBranchName())) {
                throw new BadRequestException(
                        "Cannot create tag '" + tagName + "' from branch '" + tagRes.getBranchName()
                                + "': additional repository '" + displayName
                                + "' does not have that branch. Create branch '" + tagRes.getBranchName()
                                + "' on that additional repository (or tag from a branch that exists on every repository), then retry publishing."
                );
            }
        }
    }

    private AdditionalGitRemote resolveAdditionalGitRemote(DataProductAdditionalRepo additionalRepo, HttpHeaders headers) {
        String identitySuffix = additionalRepoIdentitySuffix(additionalRepo);
        if (!StringUtils.hasText(additionalRepo.getExternalIdentifier())) {
            throw new BadRequestException("Additional repository is missing externalIdentifier" + identitySuffix);
        }
        if (!StringUtils.hasText(additionalRepo.getOwnerId())) {
            throw new BadRequestException("Additional repository is missing ownerId" + identitySuffix);
        }
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(additionalRepo.getProviderType().name(), additionalRepo.getProviderBaseUrl()),
                headers);
        Repository gitRepo = provider.getRepository(additionalRepo.getExternalIdentifier(), additionalRepo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + additionalRepo.getUuid()));
        return new AdditionalGitRemote(provider, gitRepo);
    }

    private boolean remoteHasTag(GitProvider provider, Repository repository, String tagName) {
        return pageContainsName(pageable -> provider.listTags(repository, pageable), tagName, Tag::getName);
    }

    private boolean remoteHasBranch(GitProvider provider, Repository repository, String branchName) {
        return pageContainsName(pageable -> provider.listBranches(repository, pageable), branchName, Branch::getName);
    }

    private <T> boolean pageContainsName(
            Function<Pageable, Page<T>> loader,
            String name,
            Function<T, String> nameExtractor
    ) {
        Pageable pageable = PageRequest.of(0, 100);
        while (true) {
            Page<T> page = loader.apply(pageable);
            if (page == null || page.getContent() == null) {
                return false;
            }
            if (page.getContent().stream().anyMatch(item -> name.equals(nameExtractor.apply(item)))) {
                return true;
            }
            if (!page.hasNext()) {
                return false;
            }
            pageable = page.nextPageable();
        }
    }

    private String additionalRepoDisplayName(DataProductAdditionalRepo additionalRepo) {
        if (StringUtils.hasText(additionalRepo.getManifestKey())) {
            return additionalRepo.getManifestKey();
        }
        if (StringUtils.hasText(additionalRepo.getName())) {
            return additionalRepo.getName();
        }
        return additionalRepo.getExternalIdentifier();
    }

    private void applyTagAndPush(GitProvider provider, File repository, TagRes tagRes, String targetSha) {
        provider.gitOperation().addTag(
                repository,
                new Tag(tagRes.getName(), targetSha, tagRes.getAuthorName(), tagRes.getAuthorEmail(), tagRes.getMessage())
        );
        provider.gitOperation().push(repository, true);
    }

    private String additionalRepoIdentitySuffix(DataProductAdditionalRepo additionalRepo) {
        if (StringUtils.hasText(additionalRepo.getManifestKey())) {
            return " (manifestKey: " + additionalRepo.getManifestKey() + ")";
        }
        return "";
    }

    private String retrieveTagTargetCommit(TagRes tagRes, File repository, GitProvider provider, DataProductRepo dataProductRepo) {
        String targetSha;
        if (StringUtils.hasText(tagRes.getCommitHash())) {
            // CASE 1 → Tag on explicit commit SHA
            targetSha = tagRes.getCommitHash();
        } else if (StringUtils.hasText(tagRes.getBranchName())) {
            // CASE 2 → Tag latest commit on specified branch
            targetSha = provider.gitOperation().getHeadSha(repository, tagRes.getBranchName());
        } else {
            // CASE 3 → Tag latest commit on default branch
            targetSha = provider.gitOperation().getHeadSha(repository, dataProductRepo.getDefaultBranch());
        }
        return targetSha;
    }

    /**
     * Create a Repository object from DataProductRepo information
     */
    private Repository buildRepoObject(DataProductRepo dataProductRepo) {
        return buildRepoObject(
                dataProductRepo.getExternalIdentifier(),
                dataProductRepo.getName(),
                dataProductRepo.getDescription(),
                dataProductRepo.getRemoteUrlHttp(),
                dataProductRepo.getRemoteUrlSsh(),
                dataProductRepo.getDefaultBranch(),
                dataProductRepo.getOwnerId(),
                dataProductRepo.getOwnerType()
        );
    }

    private Repository buildRepoObject(
            String externalIdentifier,
            String name,
            String description,
            String remoteUrlHttp,
            String remoteUrlSsh,
            String defaultBranch,
            String ownerId,
            DataProductRepoOwnerType ownerType
    ) {
        Repository repository = new Repository();
        repository.setId(externalIdentifier);
        repository.setName(name);
        repository.setDescription(description);
        repository.setCloneUrlHttp(remoteUrlHttp);
        repository.setCloneUrlSsh(remoteUrlSsh);
        repository.setDefaultBranch(defaultBranch);
        repository.setOwnerId(ownerId);
        if (ownerType != null) {
            repository.setOwnerType(RepositoryOwnerType.valueOf(ownerType.name()));
        }
        return repository;
    }

    /**
     * Builds a commit list filter from REST search options, applying validation and mapping
     * to the appropriate filter type (no filter, single branch, or range).
     * When only 'from' or only 'to' is provided, the default branch is used as the other bound.
     */
    private CommitListFilter buildCommitListFilterFromOptions(CommitSearchOptions options, String defaultBranchName) {
        if (options == null) {
            return CommitListNoFilter.getInstance();
        }

        boolean hasBranchName = StringUtils.hasText(options.getBranchName());
        boolean hasFromBranchName = StringUtils.hasText(options.getFromBranchName());
        boolean hasToBranchName = StringUtils.hasText(options.getToBranchName());
        boolean hasFromTag = StringUtils.hasText(options.getFromTagName());
        boolean hasToTag = StringUtils.hasText(options.getToTagName());
        boolean hasFromCommit = StringUtils.hasText(options.getFromCommitHash());
        boolean hasToCommit = StringUtils.hasText(options.getToCommitHash());

        if (hasBranchName && (hasFromBranchName || hasToBranchName || hasFromTag || hasToTag || hasFromCommit || hasToCommit)) {
            throw new BadRequestException(
                    "'branchName' cannot be used together with 'fromBranchName' or 'toBranchName' or from/to tag/commit. " +
                            "Use either branchName alone to list commits on one branch, or from/to parameters to list commits between refs.");
        }

        int parameterCount = (hasFromTag ? 1 : 0) + (hasToTag ? 1 : 0) + (hasFromCommit ? 1 : 0) + (hasToCommit ? 1 : 0)
                + (hasFromBranchName ? 1 : 0) + (hasToBranchName ? 1 : 0) + (hasBranchName ? 1 : 0);
        if (parameterCount > 2) {
            throw new BadRequestException("Maximum two parameters can be set at a time");
        }

        if (hasBranchName) {
            return new CommitListSingleBranchFilter(new CommitRefBranch(options.getBranchName()));
        }

        CommitRef fromRef = buildFromRef(options);
        CommitRef toRef = buildToRef(options);

        if (fromRef == null && toRef == null) {
            return CommitListNoFilter.getInstance();
        }
        // Single bound: use default branch as the other (from ref to HEAD, or from branch start to ref)
        if (fromRef != null && toRef == null) {
            if (!StringUtils.hasText(defaultBranchName)) {
                throw new BadRequestException("For commit range filter both 'from' and 'to' parameters are required, or configure a default branch.");
            }
            toRef = new CommitRefBranch(defaultBranchName);
        } else if (fromRef == null && toRef != null) {
            if (!StringUtils.hasText(defaultBranchName)) {
                throw new BadRequestException("For commit range filter both 'from' and 'to' parameters are required, or configure a default branch.");
            }
            fromRef = new CommitRefBranch(defaultBranchName);
        }

        return new CommitListRangeFilter(fromRef, toRef);
    }

    private CommitRef buildFromRef(CommitSearchOptions options) {
        if (StringUtils.hasText(options.getFromTagName())) {
            return new CommitRefTag(options.getFromTagName());
        }
        if (StringUtils.hasText(options.getFromCommitHash())) {
            return new CommitRefHash(options.getFromCommitHash());
        }
        if (StringUtils.hasText(options.getFromBranchName())) {
            return new CommitRefBranch(options.getFromBranchName());
        }
        return null;
    }

    private CommitRef buildToRef(CommitSearchOptions options) {
        if (StringUtils.hasText(options.getToTagName())) {
            return new CommitRefTag(options.getToTagName());
        }
        if (StringUtils.hasText(options.getToCommitHash())) {
            return new CommitRefHash(options.getToCommitHash());
        }
        if (StringUtils.hasText(options.getToBranchName())) {
            return new CommitRefBranch(options.getToBranchName());
        }
        return null;
    }

    private RepositoryPointer buildRepositoryPointer(GitReference pointer) {
        return switch (pointer.type()) {
            case TAG -> new RepositoryPointerTag(pointer.tag());
            case BRANCH -> new RepositoryPointerBranch(pointer.branch());
            case COMMIT -> new RepositoryPointerCommit(pointer.commit());
        };
    }

    private record AdditionalGitRemote(GitProvider provider, Repository gitRepo) {
    }

    private record GitReference(String tag, String branch, String commit) {
        enum VersionType { TAG, BRANCH, COMMIT }

        VersionType type() {
            if (tag != null) return VersionType.TAG;
            if (branch != null) return VersionType.BRANCH;
            if (commit != null) return VersionType.COMMIT;
            return VersionType.BRANCH;
        }
    }

}

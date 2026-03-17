package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Optional;

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
    public TagRes addTag(String dataProductUuid, TagRes tagRes, HttpHeaders headers) {
        logger.info("Adding tag for data product {}: tagName={}", dataProductUuid, tagRes.getName());
        if (!StringUtils.hasText(tagRes.getName())) {
            throw new BadRequestException("Missing tag name");
        }
        DataProductRepo dataProductRepo = Optional.ofNullable(service.findOne(dataProductUuid).getDataProductRepo())
                .orElseThrow(() -> new BadRequestException("Data product does not have an associated repository"));

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
                provider.gitOperation().addTag(
                        repository,
                        new Tag(tagRes.getName(), targetSha, tagRes.getAuthorName(), tagRes.getAuthorEmail(), tagRes.getMessage())
                );
                provider.gitOperation().push(repository, true);
            });
        } catch (GitOperationException e) {
            logger.warn("Failed to create tag for data product {}: {}", dataProductUuid, e.getMessage(), e);
            throw new BadRequestException("Failed to create tag: " + e.getMessage());
        }
        logger.info("Tag {} added successfully for data product {}", tagRes.getName(), dataProductUuid);
        return tagRes;
    }

    private String retrieveTagTargetCommit(TagRes tagRes, File repository, GitProvider provider, DataProductRepo dataProductRepo) {
        String targetSha;
        if (StringUtils.hasText(tagRes.getTarget())) {
            // CASE 1 → Tag on explicit commit SHA
            targetSha = tagRes.getTarget();
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
        Repository repository = new Repository();
        repository.setId(dataProductRepo.getExternalIdentifier());
        repository.setName(dataProductRepo.getName());
        repository.setDescription(dataProductRepo.getDescription());
        repository.setCloneUrlHttp(dataProductRepo.getRemoteUrlHttp());
        repository.setCloneUrlSsh(dataProductRepo.getRemoteUrlSsh());
        repository.setDefaultBranch(dataProductRepo.getDefaultBranch());
        repository.setOwnerId(dataProductRepo.getOwnerId());
        if (dataProductRepo.getOwnerType() != null) {
            repository.setOwnerType(RepositoryOwnerType.valueOf(dataProductRepo.getOwnerType().name()));
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

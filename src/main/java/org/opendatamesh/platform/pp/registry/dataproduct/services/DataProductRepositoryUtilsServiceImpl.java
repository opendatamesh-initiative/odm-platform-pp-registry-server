package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductAdditionalRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoOwnerType;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
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
import java.util.List;
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
    public Page<CommitRes> listCommits(String dataProductUuid, String repositoryKey, HttpHeaders headers, CommitSearchOptions searchOptions, Pageable pageable) {
        ResolvedGitRemote remote = resolveGitRemote(dataProductUuid, repositoryKey);

        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(remote.providerType().name(), remote.providerBaseUrl()),
                headers);

        Repository repository = buildRepoObject(remote);

        CommitListFilter commitListFilter = buildCommitListFilterFromOptions(searchOptions, remote.defaultBranch());

        return gitProvider.listCommits(repository, commitListFilter, pageable)
                .map(commitMapper::toRes);
    }

    @Override
    public Page<BranchRes> listBranches(String dataProductUuid, String repositoryKey, HttpHeaders headers, Pageable pageable) {
        ResolvedGitRemote remote = resolveGitRemote(dataProductUuid, repositoryKey);

        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(remote.providerType().name(), remote.providerBaseUrl()),
                headers);

        Repository repository = buildRepoObject(remote);
        return gitProvider.listBranches(repository, pageable)
                .map(branchMapper::toRes);
    }

    @Override
    public Page<TagRes> listTags(String dataProductUuid, String repositoryKey, HttpHeaders headers, Pageable pageable) {
        ResolvedGitRemote remote = resolveGitRemote(dataProductUuid, repositoryKey);

        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(remote.providerType().name(), remote.providerBaseUrl()),
                headers);
        Repository repository = buildRepoObject(remote);

        return gitProvider.listTags(repository, pageable)
                .map(tagMapper::toRes);
    }

    @Override
    public TagRes addTag(String dataProductUuid, String repositoryKey, TagRes tagRes, HttpHeaders headers) {
        logger.info("Adding tag for data product {}: tagName={}, repositoryKey={}",
                dataProductUuid, tagRes.getName(), StringUtils.hasText(repositoryKey) ? repositoryKey : "root");
        if (!StringUtils.hasText(tagRes.getName())) {
            throw new BadRequestException("Missing tag name");
        }
        ResolvedGitRemote remote = resolveGitRemote(dataProductUuid, repositoryKey);

        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(remote.providerType().name(), remote.providerBaseUrl()),
                headers);

        String branchName = StringUtils.hasText(tagRes.getBranchName()) ? tagRes.getBranchName()
                : remote.defaultBranch();

        Repository gitRepo = provider.getRepository(remote.externalIdentifier(), remote.ownerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + dataProductUuid));

        RepositoryPointer repositoryPointer = buildRepositoryPointer(new GitReference(null, branchName, null));

        try {
            provider.gitOperation().readRepository(gitRepo, repositoryPointer, repository -> {
                String targetSha = retrieveTagTargetCommit(tagRes, repository, provider, remote);
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
        logger.info("Tag {} added successfully for data product {} (repositoryKey={})",
                tagRes.getName(), dataProductUuid, StringUtils.hasText(repositoryKey) ? repositoryKey : "root");
        return tagRes;
    }

    private ResolvedGitRemote resolveGitRemote(String dataProductUuid, String repositoryKey) {
        DataProduct dataProduct = service.findOne(dataProductUuid);
        if (!StringUtils.hasText(repositoryKey)) {
            DataProductRepo dataProductRepo = Optional.ofNullable(dataProduct.getDataProductRepo())
                    .orElseThrow(() -> new BadRequestException("Data product does not have an associated repository"));
            return ResolvedGitRemote.fromRoot(dataProductRepo);
        }
        List<DataProductAdditionalRepo> additionalRepos = dataProduct.getAdditionalDataProductRepos();
        if (additionalRepos == null || additionalRepos.isEmpty()) {
            throw new BadRequestException(
                    "Data product does not have additional repositories; cannot resolve repository key: " + repositoryKey);
        }
        return additionalRepos.stream()
                .filter(repo -> repo != null && repositoryKey.equals(repo.getRepositoryKey()))
                .findFirst()
                .map(ResolvedGitRemote::fromAdditional)
                .orElseThrow(() -> new BadRequestException(
                        "No additional repository found with repository key: " + repositoryKey));
    }

    private String retrieveTagTargetCommit(TagRes tagRes, File repository, GitProvider provider, ResolvedGitRemote remote) {
        String targetSha;
        if (StringUtils.hasText(tagRes.getCommitHash())) {
            targetSha = tagRes.getCommitHash();
        } else if (StringUtils.hasText(tagRes.getBranchName())) {
            targetSha = provider.gitOperation().getHeadSha(repository, tagRes.getBranchName());
        } else {
            targetSha = provider.gitOperation().getHeadSha(repository, remote.defaultBranch());
        }
        return targetSha;
    }

    private Repository buildRepoObject(ResolvedGitRemote remote) {
        Repository repository = new Repository();
        repository.setId(remote.externalIdentifier());
        repository.setName(remote.name());
        repository.setDescription(remote.description());
        repository.setCloneUrlHttp(remote.remoteUrlHttp());
        repository.setCloneUrlSsh(remote.remoteUrlSsh());
        repository.setDefaultBranch(remote.defaultBranch());
        repository.setOwnerId(remote.ownerId());
        if (remote.ownerType() != null) {
            repository.setOwnerType(RepositoryOwnerType.valueOf(remote.ownerType().name()));
        }
        return repository;
    }

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

    private record ResolvedGitRemote(
            String externalIdentifier,
            String name,
            String description,
            String remoteUrlHttp,
            String remoteUrlSsh,
            String defaultBranch,
            DataProductRepoProviderType providerType,
            String providerBaseUrl,
            String ownerId,
            DataProductRepoOwnerType ownerType
    ) {
        static ResolvedGitRemote fromRoot(DataProductRepo repo) {
            return new ResolvedGitRemote(
                    repo.getExternalIdentifier(),
                    repo.getName(),
                    repo.getDescription(),
                    repo.getRemoteUrlHttp(),
                    repo.getRemoteUrlSsh(),
                    repo.getDefaultBranch(),
                    repo.getProviderType(),
                    repo.getProviderBaseUrl(),
                    repo.getOwnerId(),
                    repo.getOwnerType()
            );
        }

        static ResolvedGitRemote fromAdditional(DataProductAdditionalRepo repo) {
            return new ResolvedGitRemote(
                    repo.getExternalIdentifier(),
                    repo.getName(),
                    repo.getDescription(),
                    repo.getRemoteUrlHttp(),
                    repo.getRemoteUrlSsh(),
                    repo.getDefaultBranch(),
                    repo.getProviderType(),
                    repo.getProviderBaseUrl(),
                    repo.getOwnerId(),
                    repo.getOwnerType()
            );
        }
    }

}

package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperationFactory;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderIdentifier;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;

@Service
public class DataProductRepositoryUtilsServiceImpl implements DataProductRepositoryUtilsService {

    private final DataProductsService service;
    private final CommitMapper commitMapper;
    private final BranchMapper branchMapper;
    private final TagMapper tagMapper;
    private final GitProviderFactory gitProviderFactory;
    private final GitOperationFactory gitOperationFactory;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public DataProductRepositoryUtilsServiceImpl(DataProductsService service,
            CommitMapper commitMapper, BranchMapper branchMapper, TagMapper tagMapper,
                    GitProviderFactory gitProviderFactory, GitOperationFactory gitOperationFactory) {
        this.service = service;
        this.commitMapper = commitMapper;
        this.branchMapper = branchMapper;
        this.tagMapper = tagMapper;
        this.gitProviderFactory = gitProviderFactory;
        this.gitOperationFactory = gitOperationFactory;
    }

    @Override
    public Page<CommitRes> listCommits(String dataProductUuid, HttpHeaders headers, CommitSearchOptions searchOptions,
            Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = service.findOne(dataProductUuid);

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Validate Commit search options (filters) - validate early before building
        // provider
        validateCommitSearchOptions(searchOptions);

        // Create Git provider
        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(),
                        dataProductRepo.getProviderBaseUrl()),
                headers);

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        CommitPointer commitFilters = null;
        if (searchOptions != null) {
            commitFilters = new CommitPointer(
                    searchOptions.getFromTagName(),
                    searchOptions.getToTagName(),
                    searchOptions.getFromCommitHash(),
                    searchOptions.getToCommitHash(),
                    searchOptions.getFromBranchName(),
                    searchOptions.getToBranchName(),
                    searchOptions.getBranchName());
        }

        // Call the Git provider to list commits
        Page<Commit> commits = gitProvider.listCommits(repository, commitFilters, pageable);

        // Map to DTOs
        return commits.map(commitMapper::toRes);
    }

    @Override
    public Page<BranchRes> listBranches(String dataProductUuid, HttpHeaders headers, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = service.findOne(dataProductUuid);

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(),
                        dataProductRepo.getProviderBaseUrl()),
                headers);

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        // Call the Git provider to list branches
        Page<Branch> branches = gitProvider.listBranches(repository, pageable);

        // Map to DTOs
        return branches.map(branchMapper::toRes);
    }

    @Override
    public Page<TagRes> listTags(String dataProductUuid, HttpHeaders headers, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = service.findOne(dataProductUuid);

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(),
                        dataProductRepo.getProviderBaseUrl()),
                headers);

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        // Call the Git provider to list tags
        Page<Tag> tags = gitProvider.listTags(repository, pageable);

        // Map to DTOs
        return tags.map(tagMapper::toRes);
    }

    @Override
    public TagRes addTag(String dataProductUuid, TagRes tagRes, HttpHeaders headers) {
        if (!StringUtils.hasText(tagRes.getName())) {
            throw new BadRequestException("Missing tag name");
        }
        DataProductRepo dataProductRepo = service.findOne(dataProductUuid).getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("No repository configured for data product " + dataProductUuid);
        }
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(),
                        dataProductRepo.getProviderBaseUrl()),
                headers);
        String branchName = StringUtils.hasText(tagRes.getBranchName()) ? tagRes.getBranchName()
                : dataProductRepo.getDefaultBranch();
        // Always clone the default branch (safe fallback)
        RepositoryPointer repositoryPointer = buildRepositoryPointer(
                provider,
                dataProductRepo,
                new GitReference(null, branchName, null));

        var authContext = provider.createGitAuthContext();
        GitOperation gitOperation = gitOperationFactory.createGitOperation(authContext);

        File repoContent = null;
        try {
            // Clone the repository into a temporary directory
            repoContent = gitOperation.getRepositoryContent(repositoryPointer);
            // Determine which commit SHA to use
            String targetSha;
            if (StringUtils.hasText(tagRes.getTarget())) {
                // CASE 1 → Tag on explicit commit SHA
                targetSha = tagRes.getTarget();
            } else if (StringUtils.hasText(tagRes.getBranchName())) {
                // CASE 2 → Tag latest commit on specified branch
                targetSha = gitOperation.getHeadSha(repoContent, tagRes.getBranchName());
            } else {
                // CASE 3 → Tag latest commit on default branch
                targetSha = gitOperation.getHeadSha(repoContent, dataProductRepo.getDefaultBranch());
            }

            // Create the tag (annotated if message provided)
            gitOperation.addTag(
                    repoContent,
                    tagRes.getName(),
                    targetSha,
                    tagRes.getMessage(),
                    tagRes.getAuthorName(),
                    tagRes.getAuthorEmail());
            gitOperation.push(repoContent, true);
        } catch (GitOperationException e) {
            logger.warn("Failed to create tag for data product {}: {}", dataProductUuid, e.getMessage(), e);
            throw new BadRequestException("Failed to create tag: " + e.getMessage());
        } finally {
            if (repoContent != null) {
                deleteRecursively(repoContent);
            }
        }
        return tagRes;
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

    private void validateCommitSearchOptions(CommitSearchOptions commitSearchOptions) {
        if (commitSearchOptions == null)
            return;

        boolean hasBranchName = StringUtils.hasText(commitSearchOptions.getBranchName());
        boolean hasFromBranchName = StringUtils.hasText(commitSearchOptions.getFromBranchName());
        boolean hasToBranchName = StringUtils.hasText(commitSearchOptions.getToBranchName());

        // branchName is mutually exclusive with fromBranchName and toBranchName:
        if (hasBranchName && (hasFromBranchName || hasToBranchName)) {
            throw new BadRequestException(
                    "'branchName' cannot be used together with 'fromBranchName' or 'toBranchName'. " +
                            "Use either branchName alone to list commits on one branch, or fromBranchName/toBranchName to list commits between branches.");
        }

        // Count how many parameters are set (any combination is allowed, except
        // branchName with from/to branch names)
        int parameterCount = 0;
        if (StringUtils.hasText(commitSearchOptions.getFromTagName())) {
            parameterCount++;
        }
        if (StringUtils.hasText(commitSearchOptions.getToTagName())) {
            parameterCount++;
        }
        if (StringUtils.hasText(commitSearchOptions.getFromCommitHash())) {
            parameterCount++;
        }
        if (StringUtils.hasText(commitSearchOptions.getToCommitHash())) {
            parameterCount++;
        }
        if (hasFromBranchName) {
            parameterCount++;
        }
        if (hasToBranchName) {
            parameterCount++;
        }
        if (hasBranchName) {
            parameterCount++;
        }

        // Maximum two parameters can be set
        if (parameterCount > 2) {
            throw new BadRequestException("Maximum two parameters can be set at a time");
        }
    }

    private RepositoryPointer buildRepositoryPointer(GitProvider provider, DataProductRepo repo, GitReference pointer) {
        Repository gitRepo = provider.getRepository(repo.getExternalIdentifier(), repo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + repo.getUuid()));

        return switch (pointer.getType()) {
            case TAG -> new RepositoryPointerTag(gitRepo, pointer.getTag());
            case BRANCH -> new RepositoryPointerBranch(gitRepo, pointer.getBranch());
            case COMMIT -> new RepositoryPointerCommit(gitRepo, pointer.getCommit());
        };
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            logger.warn("Failed to delete temp file/folder: {}", file.getAbsolutePath());
        }
    }

}

package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.model.Branch;
import org.opendatamesh.platform.pp.registry.githandler.model.Commit;
import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Tag;
import org.opendatamesh.platform.pp.registry.githandler.model.filters.ListCommitFilters;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;


@Service
public class DataProductsUtilsServiceImpl implements DataProductUtilsService {

    private final DataProductsService service;
    private final CommitMapper commitMapper;
    private final BranchMapper branchMapper;
    private final TagMapper tagMapper;
    private final GitProviderFactory gitProviderFactory;

    @Autowired
    public DataProductsUtilsServiceImpl(DataProductsService service,
                                        CommitMapper commitMapper, BranchMapper branchMapper, TagMapper tagMapper,
                                        GitProviderFactory gitProviderFactory) {
        this.service = service;
        this.commitMapper = commitMapper;
        this.branchMapper = branchMapper;
        this.tagMapper = tagMapper;
        this.gitProviderFactory = gitProviderFactory;
    }

    @Override
    public Page<CommitRes> listCommits(String dataProductUuid, Credential credential, CommitSearchOptions searchOptions, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = service.findOne(dataProductUuid);

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Validate Commit search options (filters) - validate early before building provider
        validateCommitSearchOptions(searchOptions);

        // Create Git provider
        GitProvider gitProvider = buildGitProvider(dataProductRepo, credential);

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        ListCommitFilters commitFilters = new ListCommitFilters(
                searchOptions != null ? searchOptions.getFromTagName() : null,
                searchOptions != null ? searchOptions.getToTagName() : null,
                searchOptions != null ? searchOptions.getFromCommitHash() : null,
                searchOptions != null ? searchOptions.getToCommitHash() : null,
                searchOptions != null ? searchOptions.getFromBranchName() : null,
                searchOptions != null ? searchOptions.getToBranchName() : null
        );

        // Call the Git provider to list commits
        Page<Commit> commits = gitProvider.listCommits(repository, commitFilters, pageable);

        // Map to DTOs
        return commits.map(commitMapper::toRes);
    }

    @Override
    public Page<BranchRes> listBranches(String dataProductUuid, Credential credential, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = service.findOne(dataProductUuid);

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = buildGitProvider(dataProductRepo, credential);

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        // Call the Git provider to list branches
        Page<Branch> branches = gitProvider.listBranches(repository, pageable);

        // Map to DTOs
        return branches.map(branchMapper::toRes);
    }

    @Override
    public Page<TagRes> listTags(String dataProductUuid, Credential credential, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = service.findOne(dataProductUuid);

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = buildGitProvider(dataProductRepo, credential);

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        // Call the Git provider to list tags
        Page<Tag> tags = gitProvider.listTags(repository, pageable);

        // Map to DTOs
        return tags.map(tagMapper::toRes);
    }

    /**
     * Create a GitProvider instance from DataProductRepo information
     */
    private GitProvider buildGitProvider(DataProductRepo dataProductRepo, Credential credential) {
        // Create Git provider using the factory with the provided credentials
        Optional<GitProvider> providerOpt = gitProviderFactory.getProvider(
                dataProductRepo.getProviderType(),
                dataProductRepo.getProviderBaseUrl(),
                new RestTemplate(),
                credential
        );
        
        if (providerOpt.isEmpty()) {
            throw new BadRequestException("Unsupported provider type: " + dataProductRepo.getProviderType());
        }
        
        return providerOpt.get();
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
            repository.setOwnerType(OwnerType.valueOf(dataProductRepo.getOwnerType().name()));
        }
        return repository;
    }

    private void validateCommitSearchOptions(CommitSearchOptions commitSearchOptions) {
        if (commitSearchOptions == null) return;

        // Tag pair validation
        boolean fromTagSet = commitSearchOptions.getFromTagName() != null && !commitSearchOptions.getFromTagName().isEmpty();
        boolean toTagSet = commitSearchOptions.getToTagName() != null && !commitSearchOptions.getToTagName().isEmpty();
        if (fromTagSet ^ toTagSet) { // XOR: only one is set
            throw new BadRequestException("Both fromTagName and toTagName must be defined together");
        }

        // Commit hash pair validation
        boolean fromHashSet = commitSearchOptions.getFromCommitHash() != null && !commitSearchOptions.getFromCommitHash().isEmpty();
        boolean toHashSet = commitSearchOptions.getToCommitHash() != null && !commitSearchOptions.getToCommitHash().isEmpty();
        if (fromHashSet ^ toHashSet) {
            throw new BadRequestException("Both fromCommitHash and toCommitHash must be defined together");
        }

        // Branch pair validation
        boolean fromBranchSet = commitSearchOptions.getFromBranchName() != null && !commitSearchOptions.getFromBranchName().isEmpty();
        boolean toBranchSet = commitSearchOptions.getToBranchName() != null && !commitSearchOptions.getToBranchName().isEmpty();
        if (fromBranchSet ^ toBranchSet) {
            throw new BadRequestException("Both fromBranchName and toBranchName must be defined together");
        }

        // Ensure only **one type** of pair is set at a time
        int typeCount = 0;
        if (fromTagSet) typeCount++;
        if (fromHashSet) typeCount++;
        if (fromBranchSet) typeCount++;
        if (typeCount > 1) {
            throw new BadRequestException("Only one type of comparison can be used at a time (tags, commit hashes, or branches)");
        }
    }
}

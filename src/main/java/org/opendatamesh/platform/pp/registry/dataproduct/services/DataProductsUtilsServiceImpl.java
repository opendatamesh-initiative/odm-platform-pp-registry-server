package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderIdentifier;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;


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
    public Page<CommitRes> listCommits(String dataProductUuid, HttpHeaders headers, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = service.findOne(dataProductUuid);

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        // Call the Git provider to list commits
        Page<Commit> commits = gitProvider.listCommits(repository, pageable);

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
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );

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
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );

        // Create Repository object for the Git provider
        Repository repository = buildRepoObject(dataProductRepo);

        // Call the Git provider to list tags
        Page<Tag> tags = gitProvider.listTags(repository, pageable);

        // Map to DTOs
        return tags.map(tagMapper::toRes);
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
}

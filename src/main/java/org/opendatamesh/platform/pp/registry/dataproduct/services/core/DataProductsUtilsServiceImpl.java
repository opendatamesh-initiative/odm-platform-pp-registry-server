package org.opendatamesh.platform.pp.registry.dataproduct.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.model.Branch;
import org.opendatamesh.platform.pp.registry.githandler.model.Commit;
import org.opendatamesh.platform.pp.registry.githandler.model.Organization;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Tag;
import org.opendatamesh.platform.pp.registry.githandler.model.User;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
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
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;

    @Autowired
    public DataProductsUtilsServiceImpl(DataProductsService service,
                                        CommitMapper commitMapper, BranchMapper branchMapper, TagMapper tagMapper,
                                        GitProviderFactory gitProviderFactory, UserMapper userMapper, OrganizationMapper organizationMapper) {
        this.service = service;
        this.commitMapper = commitMapper;
        this.branchMapper = branchMapper;
        this.tagMapper = tagMapper;
        this.gitProviderFactory = gitProviderFactory;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
    }

    @Override
    public Page<CommitRes> listCommits(String dataProductUuid, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable) {
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

        // Convert UserRes and OrganizationRes to domain objects using mappers
        User user = userMapper.toEntity(userRes);
        Organization org = null;
        if (organizationRes != null) {
            org = organizationMapper.toEntity(organizationRes);
        }

        // Call the Git provider to list commits
        Page<Commit> commits = gitProvider.listCommits(org, user, repository, pageable);

        // Map to DTOs
        return commits.map(commitMapper::toRes);
    }

    @Override
    public Page<BranchRes> listBranches(String dataProductUuid, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable) {
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

        // Convert UserRes and OrganizationRes to domain objects using mappers
        User user = userMapper.toEntity(userRes);
        Organization org = null;
        if (organizationRes != null) {
            org = organizationMapper.toEntity(organizationRes);
        }

        // Call the Git provider to list branches
        Page<Branch> branches = gitProvider.listBranches(org, user, repository, pageable);

        // Map to DTOs
        return branches.map(branchMapper::toRes);
    }

    @Override
    public Page<TagRes> listTags(String dataProductUuid, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable) {
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

        // Convert UserRes and OrganizationRes to domain objects using mappers
        User user = userMapper.toEntity(userRes);
        Organization org = null;
        if (organizationRes != null) {
            org = organizationMapper.toEntity(organizationRes);
        }

        // Call the Git provider to list tags
        Page<Tag> tags = gitProvider.listTags(org, user, repository, pageable);

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
        return repository;
    }
}

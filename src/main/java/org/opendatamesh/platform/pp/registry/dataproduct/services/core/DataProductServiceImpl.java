package org.opendatamesh.platform.pp.registry.dataproduct.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.dataproduct.repositories.DataProductRepository;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.ResourceConflictException;
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.githandler.model.Branch;
import org.opendatamesh.platform.pp.registry.githandler.model.Commit;
import org.opendatamesh.platform.pp.registry.githandler.model.Organization;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Tag;
import org.opendatamesh.platform.pp.registry.githandler.model.User;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagRes;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.opendatamesh.platform.pp.registry.utils.services.GenericMappedAndFilteredCrudServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class DataProductServiceImpl extends GenericMappedAndFilteredCrudServiceImpl<DataProductSearchOptions, DataProductRes, DataProduct, String> implements DataProductService {

    private final DataProductMapper mapper;
    private final DataProductRepository repository;
    private final CommitMapper commitMapper;
    private final BranchMapper branchMapper;
    private final TagMapper tagMapper;
    private final GitProviderFactory gitProviderFactory;

    @Autowired
    public DataProductServiceImpl(DataProductMapper mapper, DataProductRepository repository, 
                                 CommitMapper commitMapper, BranchMapper branchMapper, TagMapper tagMapper,
                                 GitProviderFactory gitProviderFactory) {
        this.mapper = mapper;
        this.repository = repository;
        this.commitMapper = commitMapper;
        this.branchMapper = branchMapper;
        this.tagMapper = tagMapper;
        this.gitProviderFactory = gitProviderFactory;
    }


    @Override
    protected PagingAndSortingAndSpecificationExecutorRepository<DataProduct, String> getRepository() {
        return repository;
    }

    @Override
    protected void validate(DataProduct objectToValidate) {
        if (objectToValidate == null) {
            throw new BadRequestException("DataProduct cannot be null");
        }

        // Validate required fields
        validateRequiredFields(objectToValidate);

        // Validate field constraints
        validateFieldConstraints(objectToValidate);

        // Validate nested DataProductRepo if present
        if (objectToValidate.getDataProductRepository() != null) {
            validateDataProductRepo(objectToValidate.getDataProductRepository());
        }
    }

    private void validateRequiredFields(DataProduct dataProduct) {
        validateRequired("FQN", dataProduct.getFqn());
        validateRequired("Name", dataProduct.getName());
        validateRequired("Domain", dataProduct.getDomain());
    }

    private void validateFieldConstraints(DataProduct dataProduct) {
        validateLength("FQN", dataProduct.getFqn(), 255);
        validateLength("Domain", dataProduct.getDomain(), 255);
        validateLength("Name", dataProduct.getName(), 255);
        validateLength("Display name", dataProduct.getDisplayName(), 255);
    }

    private void validateDataProductRepo(DataProductRepo dataProductRepo) {
        if (dataProductRepo == null) return;

        // Required fields (except description)
        validateRequired("Repository name", dataProductRepo.getName());
        validateRequired("External identifier", dataProductRepo.getExternalIdentifier());
        validateRequired("Descriptor root path", dataProductRepo.getDescriptorRootPath());
        validateRequired("HTTP remote URL", dataProductRepo.getRemoteUrlHttp());
        validateRequired("SSH remote URL", dataProductRepo.getRemoteUrlSsh());
        validateRequired("Default branch", dataProductRepo.getDefaultBranch());
        validateRequired("Provider base URL", dataProductRepo.getProviderBaseUrl());

        if (dataProductRepo.getProviderType() == null) {
            throw new BadRequestException("Provider type is required");
        }

        // Validate provider type
        try {
            DataProductRepoProviderType.fromString(dataProductRepo.getProviderType().name());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid provider type: " + dataProductRepo.getProviderType());
        }

        // Length constraints
        validateLength("Repository name", dataProductRepo.getName(), 255);
        validateLength("External identifier", dataProductRepo.getExternalIdentifier(), 255);
        validateLength("Default branch", dataProductRepo.getDefaultBranch(), 255);
        validateLength("Descriptor root path", dataProductRepo.getDescriptorRootPath(), 500);
        validateLength("HTTP remote URL", dataProductRepo.getRemoteUrlHttp(), 500);
        validateLength("SSH remote URL", dataProductRepo.getRemoteUrlSsh(), 500);
        validateLength("Provider base URL", dataProductRepo.getProviderBaseUrl(), 500);
    }

    private void validateRequired(String fieldName, String value) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(fieldName + " is required");
        }
    }

    private void validateLength(String fieldName, String value, int maxLength) {
        if (StringUtils.hasText(value) && value.length() > maxLength) {
            throw new BadRequestException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }


    @Override
    protected void reconcile(DataProduct objectToReconcile) {
        if (objectToReconcile == null) {
            return;
        }

        // Reconcile nested DataProductRepo if present
        if (objectToReconcile.getDataProductRepository() != null) {
            reconcileDataProductRepo(objectToReconcile.getDataProductRepository(), objectToReconcile);
        }
    }

    private void reconcileDataProductRepo(DataProductRepo dataProductRepo, DataProduct parentDataProduct) {
        // Set the parent DataProduct reference
        dataProductRepo.setDataProduct(parentDataProduct);

        // Set the dataProductUuid to maintain consistency
        if (parentDataProduct.getUuid() != null) {
            dataProductRepo.setDataProductUuid(parentDataProduct.getUuid());
        }

    }

    @Override
    protected Specification<DataProduct> getSpecFromFilters(DataProductSearchOptions filters) {
        List<Specification<DataProduct>> specs = new ArrayList<>();

        if (filters != null) {
            if (StringUtils.hasText(filters.getDomain())) {
                specs.add(DataProductRepository.Specs.hasDomain(filters.getDomain()));
            }
            if (StringUtils.hasText(filters.getName())) {
                specs.add(DataProductRepository.Specs.hasName(filters.getName()));
            }
            if (StringUtils.hasText(filters.getFqn())) {
                specs.add(DataProductRepository.Specs.hasFqn(filters.getFqn()));
            }
        }

        return SpecsUtils.combineWithAnd(specs);
    }

    @Override
    protected DataProductRes toRes(DataProduct entity) {
        return mapper.toRes(entity);
    }

    @Override
    protected DataProduct toEntity(DataProductRes resource) {
        return mapper.toEntity(resource);
    }

    @Override
    protected void beforeCreation(DataProduct objectToCreate) {
        validateNaturalKeyConstraints(objectToCreate, null);
    }

    @Override
    protected void beforeOverwrite(DataProduct objectToOverwrite) {
        // For overwrite, we need to validate uniqueness excluding the current entity
        validateNaturalKeyConstraints(objectToOverwrite, objectToOverwrite.getUuid());
    }

    @Override
    public DataProductRes overwriteResource(String id, DataProductRes resource) {
        // Force the UUID in the resource to match the path parameter
        resource.setUuid(id);
        return super.overwriteResource(id, resource);
    }

    /**
     * Validates uniqueness constraints for name+domain and fqn
     *
     * @param dataProduct the data product to validate
     * @param excludeUuid UUID to exclude from uniqueness check (for updates)
     */
    private void validateNaturalKeyConstraints(DataProduct dataProduct, String excludeUuid) {
        // Validate name+domain uniqueness
        Optional<DataProduct> existingByNameAndDomain;
        if (StringUtils.hasText(excludeUuid)) {
            existingByNameAndDomain = repository.findByNameIgnoreCaseAndDomainIgnoreCaseAndUuidNot(
                    dataProduct.getName(), dataProduct.getDomain(), excludeUuid);
        } else {
            existingByNameAndDomain = repository.findByNameIgnoreCaseAndDomainIgnoreCase(
                    dataProduct.getName(), dataProduct.getDomain());
        }

        if (existingByNameAndDomain.isPresent()) {
            throw new ResourceConflictException(
                    String.format("A data product with name '%s' and domain '%s' already exists",
                            dataProduct.getName(), dataProduct.getDomain()));
        }

        // Validate FQN uniqueness
        Optional<DataProduct> existingByFqn;
        if (StringUtils.hasText(excludeUuid)) {
            existingByFqn = repository.findByFqnIgnoreCaseAndUuidNot(dataProduct.getFqn(), excludeUuid);
        } else {
            existingByFqn = repository.findByFqnIgnoreCase(dataProduct.getFqn());
        }

        if (existingByFqn.isPresent()) {
            throw new ResourceConflictException(
                    String.format("A data product with FQN '%s' already exists", dataProduct.getFqn()));
        }
    }

    @Override
    public Page<CommitRes> listCommits(String dataProductUuid, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = repository.findById(dataProductUuid)
                .orElseThrow(() -> new NotFoundException("Data product not found with UUID: " + dataProductUuid));

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepository();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = createGitProvider(dataProductRepo, credential);

        // Create Repository object for the Git provider
        Repository repository = createRepositoryFromDataProductRepo(dataProductRepo);

        // Create Organization and User objects from request parameters
        Organization org = null;
        if (organizationId != null && !organizationId.trim().isEmpty()) {
            org = new Organization();
            org.setId(organizationId);
            org.setName(organizationName != null ? organizationName : organizationId);
        }
        
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        // Call the Git provider to list commits
        Page<Commit> commits = gitProvider.listCommits(org, user, repository, pageable);

        // Map to DTOs
        return commits.map(commitMapper::toRes);
    }

    @Override
    public Page<BranchRes> listBranches(String dataProductUuid, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = repository.findById(dataProductUuid)
                .orElseThrow(() -> new NotFoundException("Data product not found with UUID: " + dataProductUuid));

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepository();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = createGitProvider(dataProductRepo, credential);

        // Create Repository object for the Git provider
        Repository repository = createRepositoryFromDataProductRepo(dataProductRepo);

        // Create Organization and User objects from request parameters
        Organization org = null;
        if (organizationId != null && !organizationId.trim().isEmpty()) {
            org = new Organization();
            org.setId(organizationId);
            org.setName(organizationName != null ? organizationName : organizationId);
        }
        
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        // Call the Git provider to list branches
        Page<Branch> branches = gitProvider.listBranches(org, user, repository, pageable);

        // Map to DTOs
        return branches.map(branchMapper::toRes);
    }

    @Override
    public Page<TagRes> listTags(String dataProductUuid, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable) {
        // Find the data product
        DataProduct dataProduct = repository.findById(dataProductUuid)
                .orElseThrow(() -> new NotFoundException("Data product not found with UUID: " + dataProductUuid));

        // Check if data product has a repository
        DataProductRepo dataProductRepo = dataProduct.getDataProductRepository();
        if (dataProductRepo == null) {
            throw new BadRequestException("Data product does not have an associated repository");
        }

        // Create Git provider
        GitProvider gitProvider = createGitProvider(dataProductRepo, credential);

        // Create Repository object for the Git provider
        Repository repository = createRepositoryFromDataProductRepo(dataProductRepo);

        // Create Organization and User objects from request parameters
        Organization org = null;
        if (organizationId != null && !organizationId.trim().isEmpty()) {
            org = new Organization();
            org.setId(organizationId);
            org.setName(organizationName != null ? organizationName : organizationId);
        }
        
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        // Call the Git provider to list tags
        Page<Tag> tags = gitProvider.listTags(org, user, repository, pageable);

        // Map to DTOs
        return tags.map(tagMapper::toRes);
    }

    /**
     * Create a GitProvider instance from DataProductRepo information
     */
    private GitProvider createGitProvider(DataProductRepo dataProductRepo, PatCredential credential) {
        // Create Git provider using the factory with the provided credentials
        return gitProviderFactory.getProvider(
                dataProductRepo.getProviderType(),
                dataProductRepo.getProviderBaseUrl(),
                new RestTemplate(),
                credential
        );
    }

    /**
     * Create a Repository object from DataProductRepo information
     */
    private Repository createRepositoryFromDataProductRepo(DataProductRepo dataProductRepo) {
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

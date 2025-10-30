package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.*;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class GitProviderServiceImpl implements GitProviderService {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private RepositoryMapper repositoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GitProviderFactory gitProviderFactory;

    @Autowired
    private BranchMapper branchMapper;

    @Override
    public Page<OrganizationRes> listOrganizations(ProviderIdentifierRes providerIdentifier, Credential credential, Pageable pageable) {
        GitProvider provider = getGitProvider(providerIdentifier, credential);
        
        Page<Organization> organizations = provider.listOrganizations(pageable);
        
        return organizations.map(organizationMapper::toRes);
    }

    @Override
    public Page<RepositoryRes> listRepositories(ProviderIdentifierRes providerIdentifier, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable) {
        GitProvider provider = getGitProvider(providerIdentifier, credential);
        
        User user = userMapper.toEntity(userRes);
        Organization org = organizationRes != null ? organizationMapper.toEntity(organizationRes) : null;
        
        Page<Repository> repositories = provider.listRepositories(org, user, pageable);
        
        return repositories.map(repositoryMapper::toRes);
    }

    @Override
    public RepositoryRes createRepository(ProviderIdentifierRes providerIdentifier, UserRes userRes, OrganizationRes organizationRes, Credential credential, CreateRepositoryReqRes createRepositoryReqRes) {
        GitProvider provider = getGitProvider(providerIdentifier, credential);
        
        validateCreateRepositoryReqRes(createRepositoryReqRes);
        
        User user = userMapper.toEntity(userRes);
        Organization org = organizationRes != null ? organizationMapper.toEntity(organizationRes) : null;
        
        Repository repositoryToCreate = buildRepositoryObject(createRepositoryReqRes, user, org);
        
        Repository createdRepository = provider.createRepository(repositoryToCreate);
        
        return repositoryMapper.toRes(createdRepository);
    }

    @Override
    public Page<BranchRes> listBranches(ProviderIdentifierRes providerIdentifier, String repositoryId, Credential credential, Pageable pageable) {
        GitProvider provider = getGitProvider(providerIdentifier, credential);
        
        // Get repository information first
        Optional<Repository> repositoryOpt = provider.getRepository(repositoryId);
        if (repositoryOpt.isEmpty()) {
            throw new BadRequestException("Repository not found with ID: " + repositoryId);
        }
        
        Repository repository = repositoryOpt.get();
        
        // Call the Git provider to list branches
        Page<Branch> branches = provider.listBranches(repository, pageable);
        
        // Map to DTOs
        return branches.map(branchMapper::toRes);
    }

    /**
     * Validates the provider type and gets the appropriate Git provider
     */
    private GitProvider getGitProvider(ProviderIdentifierRes providerIdentifier, Credential credential) {
        // Validate provider type
        DataProductRepoProviderType type;
        try {
            type = DataProductRepoProviderType.fromString(providerIdentifier.getProviderType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported provider type: " + providerIdentifier.getProviderType());
        }
        
        // Get the appropriate Git provider
        Optional<GitProvider> providerOpt = gitProviderFactory.getProvider(
            type,
            providerIdentifier.getProviderBaseUrl(),
            new RestTemplate(),
            credential
        );
        
        if (providerOpt.isEmpty()) {
            throw new BadRequestException("Unsupported provider type: " + providerIdentifier.getProviderType());
        }
        
        return providerOpt.get();
    }

    /**
     * Validates the CreateRepositoryReqRes object
     */
    private void validateCreateRepositoryReqRes(CreateRepositoryReqRes createRepositoryReqRes) {
        if (!StringUtils.hasText(createRepositoryReqRes.getName())) {
            throw new BadRequestException("Repository name is required and cannot be empty");
        }
        if (createRepositoryReqRes.getIsPrivate() == null) {
            throw new BadRequestException("Repository visibility (isPrivate) is required and cannot be null");
        }
    }

    /**
     * Creates a Repository domain object from the request and user/organization information
     */
    private Repository buildRepositoryObject(CreateRepositoryReqRes createRepositoryReqRes, User user, Organization org) {
        Repository repositoryToCreate = new Repository();
        repositoryToCreate.setName(createRepositoryReqRes.getName());
        repositoryToCreate.setDescription(createRepositoryReqRes.getDescription());
        repositoryToCreate.setVisibility(createRepositoryReqRes.getIsPrivate() ? 
            Visibility.PRIVATE : 
            Visibility.PUBLIC);
        
        // Set owner information
        if (org != null) {
            repositoryToCreate.setOwnerType(OwnerType.ORGANIZATION);
            repositoryToCreate.setOwnerId(org.getId());
        } else {
            repositoryToCreate.setOwnerType(OwnerType.ACCOUNT);
            repositoryToCreate.setOwnerId(user.getId());
        }
        
        return repositoryToCreate;
    }
}

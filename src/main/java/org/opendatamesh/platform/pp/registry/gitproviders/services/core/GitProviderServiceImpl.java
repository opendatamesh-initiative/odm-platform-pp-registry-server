package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderIdentifierRes;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.model.Organization;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.User;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

    @Override
    public Page<OrganizationRes> listOrganizations(ProviderIdentifierRes providerIdentifier, Credential credential, Pageable pageable) {
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
        
        GitProvider provider = providerOpt.get();
        
        // Call the provider to list organizations
        Page<Organization> organizations = provider.listOrganizations(pageable);
        
        // Map the result to DTOs
        return organizations.map(organizationMapper::toRes);
    }

    @Override
    public Page<RepositoryRes> listRepositories(ProviderIdentifierRes providerIdentifier, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable) {
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
        
        GitProvider provider = providerOpt.get();
        
        // Convert UserRes and OrganizationRes to domain objects using mappers
        User user = userMapper.toEntity(userRes);
        Organization org = null;
        if (organizationRes != null) {
            org = organizationMapper.toEntity(organizationRes);
        }
        
        // Call the provider to list repositories
        Page<Repository> repositories = provider.listRepositories(org, user, pageable);
        
        // Map the result to DTOs
        return repositories.map(repositoryMapper::toRes);
    }
}

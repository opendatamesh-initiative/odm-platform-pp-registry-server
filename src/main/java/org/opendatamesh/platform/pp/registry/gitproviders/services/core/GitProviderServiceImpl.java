package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
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

@Service
public class GitProviderServiceImpl implements GitProviderService {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private RepositoryMapper repositoryMapper;

    @Autowired
    private GitProviderFactory gitProviderFactory;

    @Override
    public Page<OrganizationRes> listOrganizations(String providerType, String providerBaseUrl, Pageable pageable, PatCredential credential) {
        // Validate provider type
        DataProductRepoProviderType type;
        try {
            type = DataProductRepoProviderType.fromString(providerType);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported provider type: " + providerType);
        }
        
        // Get the appropriate Git provider
        GitProvider provider = gitProviderFactory.getProvider(
            type,
            providerBaseUrl,
            new RestTemplate(),
            credential
        );
        
        // Call the provider to list organizations
        Page<Organization> organizations = provider.listOrganizations(pageable);
        
        // Map the result to DTOs
        return organizations.map(organizationMapper::toRes);
    }

    @Override
    public Page<RepositoryRes> listRepositories(String providerType, String providerBaseUrl, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable) {
        // Validate provider type
        DataProductRepoProviderType type;
        try {
            type = DataProductRepoProviderType.fromString(providerType);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported provider type: " + providerType);
        }
        
        // Get the appropriate Git provider
        GitProvider provider = gitProviderFactory.getProvider(
            type,
            providerBaseUrl,
            new RestTemplate(),
            credential
        );
        
        // Create User object from parameters
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        
        // Create Organization object from parameters (if provided)
        Organization org = null;
        if (organizationId != null && !organizationId.trim().isEmpty()) {
            org = new Organization();
            org.setId(organizationId);
            org.setName(organizationName != null ? organizationName : organizationId);
        }
        
        // Call the provider to list repositories
        Page<Repository> repositories = provider.listRepositories(org, user, pageable);
        
        // Map the result to DTOs
        return repositories.map(repositoryMapper::toRes);
    }
}

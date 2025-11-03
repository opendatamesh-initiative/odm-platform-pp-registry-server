package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderIdentifierRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.CreateRepositoryReqRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderCustomResourcesDefinitionsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderCustomResourceRes;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GitProviderService {

    /**
     * List organizations from a Git provider with pagination
     *
     * @param providerIdentifier the provider identifier information
     * @param credential the personal access token credential
     * @param pageable pagination information
     * @return page of organizations
     */
    Page<OrganizationRes> listOrganizations(ProviderIdentifierRes providerIdentifier, Credential credential, Pageable pageable);

    /**
     * List repositories from a Git provider with pagination
     *
     * @param providerIdentifier the provider identifier information
     * @param userRes the user information
     * @param organizationRes the organization information (can be null for user repositories)
     * @param parameters filter parameters for the repositories (e.g., project filter)
     * @param credential the personal access token credential
     * @param pageable pagination information
     * @return page of repositories
     */
    Page<RepositoryRes> listRepositories(ProviderIdentifierRes providerIdentifier, UserRes userRes, OrganizationRes organizationRes, org.springframework.util.MultiValueMap<String, String> parameters, Credential credential, Pageable pageable);

    /**
     * Create a new repository in a Git provider
     *
     * @param providerIdentifier the provider identifier information
     * @param userRes the user information
     * @param organizationRes the organization information (can be null for user repositories)
     * @param credential the personal access token credential
     * @param createRepositoryReqRes the repository creation request
     * @return the created repository
     */
    RepositoryRes createRepository(ProviderIdentifierRes providerIdentifier, UserRes userRes, OrganizationRes organizationRes, Credential credential, CreateRepositoryReqRes createRepositoryReqRes);

    /**
     * Get custom resource definitions for a specific resource type with a given provider
     *
     * @param providerIdentifier the provider identifier information
     * @param resourceType the resource type
     * @return the custom resource definitions
     */
    ProviderCustomResourcesDefinitionsRes getProviderCustomResourcesDefinitions(ProviderIdentifierRes providerIdentifier, String resourceType);

    /**
     * Get provider-specific custom resources for a given resource type with pagination
     *
     * @param providerIdentifier the provider identifier information
     * @param customResourceType the type of custom resource to retrieve (e.g., "project", "workspace")
     * @param parameters filter parameters for the custom resources (e.g., organization filter)
     * @param credential the personal access token credential
     * @param pageable pagination information
     * @return a paginated list of provider-specific custom resources
     */
    Page<ProviderCustomResourceRes> getProviderCustomResources(ProviderIdentifierRes providerIdentifier, String customResourceType, org.springframework.util.MultiValueMap<String, String> parameters, Credential credential, Pageable pageable);
}

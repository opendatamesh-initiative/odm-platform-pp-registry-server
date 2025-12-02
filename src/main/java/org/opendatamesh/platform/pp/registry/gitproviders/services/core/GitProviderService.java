package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

/**
 * Service interface for interacting with Git providers (GitHub, GitLab, Bitbucket, Azure DevOps).
 * <p>
 * This service provides methods to list organizations, repositories, branches, and other resources
 * from various Git providers using standardized authentication via HTTP headers.
 */
public interface GitProviderService {

    /**
     * List organizations from a Git provider with pagination.
     *
     * @param providerIdentifier the provider identifier information (type, baseUrl)
     * @param headers            HTTP headers containing authentication credentials
     * @param pageable           pagination information
     * @return page of organizations
     */
    Page<OrganizationRes> listOrganizations(ProviderIdentifierRes providerIdentifier, HttpHeaders headers, Pageable pageable);

    /**
     * List repositories from a Git provider with pagination.
     *
     * @param providerIdentifier   the provider identifier information (type, baseUrl)
     * @param showUserRepositories whether to show user repositories (true) or organization repositories (false)
     * @param organizationRes      the organization information (can be null for user repositories)
     * @param parameters           filter parameters for the repositories (e.g., project filter)
     * @param headers              HTTP headers containing authentication credentials
     * @param pageable             pagination information
     * @return page of repositories
     */
    Page<RepositoryRes> listRepositories(ProviderIdentifierRes providerIdentifier, boolean showUserRepositories, OrganizationRes organizationRes, MultiValueMap<String, String> parameters, HttpHeaders headers, Pageable pageable);

    /**
     * Create a new repository in a Git provider.
     *
     * @param providerIdentifier     the provider identifier information (type, baseUrl)
     * @param organizationRes        the organization information (can be null for user repositories)
     * @param headers                HTTP headers containing authentication credentials
     * @param createRepositoryReqRes the repository creation request
     * @return the created repository
     */
    RepositoryRes createRepository(ProviderIdentifierRes providerIdentifier, OrganizationRes organizationRes, HttpHeaders headers, CreateRepositoryReqRes createRepositoryReqRes);

    /**
     * List branches from a Git provider repository with pagination.
     *
     * @param providerIdentifier the provider identifier information (type, baseUrl)
     * @param repositoryId       the repository ID
     * @param ownerId            the owner ID (organization or user ID)
     * @param headers            HTTP headers containing authentication credentials
     * @param pageable           pagination information
     * @return page of branches
     */
    Page<BranchRes> listBranches(ProviderIdentifierRes providerIdentifier, String repositoryId, String ownerId, HttpHeaders headers, Pageable pageable);

    /**
     * Get custom resource definitions for a specific resource type with a given provider.
     * <p>
     * Custom resources are provider-specific resources (e.g., Bitbucket projects, Azure DevOps projects)
     * that extend the standard Git provider model.
     *
     * @param providerIdentifier the provider identifier information (type, baseUrl)
     * @param resourceType       the resource type (e.g., "project", "workspace")
     * @return the custom resource definitions
     */
    ProviderCustomResourcesDefinitionsRes getProviderCustomResourcesDefinitions(ProviderIdentifierRes providerIdentifier, String resourceType);

    /**
     * Get provider-specific custom resources for a given resource type with pagination.
     * <p>
     * Custom resources are provider-specific resources (e.g., Bitbucket projects, Azure DevOps projects)
     * that extend the standard Git provider model.
     *
     * @param providerIdentifier the provider identifier information (type, baseUrl)
     * @param customResourceType the type of custom resource to retrieve (e.g., "project", "workspace")
     * @param parameters         filter parameters for the custom resources (e.g., organization filter)
     * @param headers            HTTP headers containing authentication credentials
     * @param pageable           pagination information
     * @return a paginated list of provider-specific custom resources
     */
    Page<ProviderCustomResourceRes> getProviderCustomResources(ProviderIdentifierRes providerIdentifier, String customResourceType, org.springframework.util.MultiValueMap<String, String> parameters, HttpHeaders headers, Pageable pageable);
}

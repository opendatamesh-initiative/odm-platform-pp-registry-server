package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GitProviderService {

    /**
     * List organizations from a Git provider with pagination
     *
     * @param providerType the type of the Git provider (e.g., "github", "gitlab", "bitbucket")
     * @param providerBaseUrl the base URL of the Git provider
     * @param pageable pagination information
     * @param credential the personal access token credential
     * @return page of organizations
     */
    Page<OrganizationRes> listOrganizations(String providerType, String providerBaseUrl, Pageable pageable, PatCredential credential);

    /**
     * List repositories from a Git provider with pagination
     *
     * @param providerType the type of the Git provider
     * @param providerBaseUrl the base URL of the Git provider
     * @param userId the ID of the user making the request
     * @param username the username of the user making the request
     * @param organizationId the organization ID (can be null for user repositories)
     * @param organizationName the organization name (can be null for user repositories)
     * @param credential the personal access token credential
     * @param pageable pagination information
     * @return page of repositories
     */
    Page<RepositoryRes> listRepositories(String providerType, String providerBaseUrl, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable);
}

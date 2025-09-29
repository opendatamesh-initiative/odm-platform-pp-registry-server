package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderIdentifierRes;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
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
    Page<OrganizationRes> listOrganizations(ProviderIdentifierRes providerIdentifier, PatCredential credential, Pageable pageable);

    /**
     * List repositories from a Git provider with pagination
     *
     * @param providerIdentifier the provider identifier information
     * @param userRes the user information
     * @param organizationRes the organization information (can be null for user repositories)
     * @param credential the personal access token credential
     * @param pageable pagination information
     * @return page of repositories
     */
    Page<RepositoryRes> listRepositories(ProviderIdentifierRes providerIdentifier, UserRes userRes, OrganizationRes organizationRes, PatCredential credential, Pageable pageable);
}

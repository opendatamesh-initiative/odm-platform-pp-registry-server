package org.opendatamesh.platform.pp.registry.gitproviders.services.core;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderIdentifierRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.CreateRepositoryReqRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
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
     * @param credential the personal access token credential
     * @param pageable pagination information
     * @return page of repositories
     */
    Page<RepositoryRes> listRepositories(ProviderIdentifierRes providerIdentifier, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable);

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
     * List branches from a Git provider repository with pagination
     *
     * @param providerIdentifier the provider identifier information
     * @param repositoryId the repository ID
     * @param credential the personal access token credential
     * @param pageable pagination information
     * @return page of branches
     */
    Page<BranchRes> listBranches(ProviderIdentifierRes providerIdentifier, String repositoryId, Credential credential, Pageable pageable);
}

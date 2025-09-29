package org.opendatamesh.platform.pp.registry.githandler.provider;


import org.opendatamesh.platform.pp.registry.githandler.model.Organization;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryPointer;
import org.opendatamesh.platform.pp.registry.githandler.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.util.Optional;

/**
 * Interface defining operations for Git providers
 */
public interface GitProvider {

    void checkConnection();

    /**
     * Get the current authenticated user
     *
     * @return the current user
     */
    User getCurrentUser();

    /**
     * List organizations with pagination
     *
     * @param page pagination information
     * @return page of organizations
     */
    Page<Organization> listOrganizations(Pageable page);

    /**
     * Get an organization by its ID
     *
     * @param id the organization ID
     * @return optional organization
     */
    Optional<Organization> getOrganization(String id);

    /**
     * List members of an organization with pagination
     *
     * @param org  the organization
     * @param page pagination information
     * @return page of users
     */
    Page<User> listMembers(Organization org, Pageable page);

    /**
     * List repositories for an organization or user with pagination
     *
     * @param org  the organization (can be null for user repositories)
     * @param usr  the user
     * @param page pagination information
     * @return page of repositories
     */
    Page<Repository> listRepositories(Organization org, User usr, Pageable page);

    /**
     * Get a repository by its ID
     *
     * @param id the repository ID
     * @return optional repository
     */
    Optional<Repository> getRepository(String id);

    Repository createRepository(Repository repositoryToCreate);

    default File readRepository(RepositoryPointer pointer) {
        throw new IllegalStateException("Not yet implemented");
    }
}

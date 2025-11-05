package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
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
     * @param org        the organization (can be null for user repositories)
     * @param usr        the user
     * @param parameters filter parameters for the repositories (e.g., project filter)
     * @param page       pagination information
     * @return page of repositories
     */
    Page<Repository> listRepositories(Organization org, User usr, MultiValueMap<String, String> parameters, Pageable page);

    /**
     * Get a repository by its ID
     *
     * @param id      the repository ID
     * @param ownerId the owner ID (required for some providers, can be null for others)
     * @return optional repository
     */
    Optional<Repository> getRepository(String id, String ownerId);

    /**
     * Create a repository
     *
     * @param repositoryToCreate the repository to create
     * @return the created repository
     */
    Repository createRepository(Repository repositoryToCreate);

    /**
     * List commits for a repository with pagination
     *
     * @param repository the repository
     * @param pageable   pagination information
     * @return page of commits
     */
    Page<Commit> listCommits(Repository repository, Pageable pageable);

    /**
     * List branches for a repository with pagination
     *
     * @param repository the repository
     * @param pageable   pagination information
     * @return page of branches
     */
    Page<Branch> listBranches(Repository repository, Pageable pageable);

    /**
     * List tags for a repository with pagination
     *
     * @param repository the repository
     * @param pageable   pagination information
     * @return page of tags
     */
    Page<Tag> listTags(Repository repository, Pageable pageable);

    /**
     * Creates a GitAuthContext based on the available credentials in this provider
     *
     * @return configured GitAuthContext
     */
    GitAuthContext createGitAuthContext();

    /**
     * Get custom definition for a specific resource type supported by this provider.
     * Each Git provider may require provider-specific properties when creating or managing resources
     * (e.g., workspace for Bitbucket repositories, visibility settings for GitHub repositories).
     *
     * @param modelResourceType the resource type to get property definitions for
     * @return list of resource definitions, each containing name, type, and required flag.
     * Returns an empty list if no additional properties are defined for the given resource type
     */
    default List<ProviderCustomResourceDefinition> getProviderCustomResourceDefinitions(GitProviderModelResourceType modelResourceType) {
        return Collections.emptyList();
    }

    /**
     * Get provider-specific custom resources for a given resource type.
     * Each Git provider may expose provider-specific resources that are not part of the standard
     * Git model (e.g., Bitbucket projects, GitHub projects, GitLab groups).
     *
     * @param customResourceType the type of custom resource to retrieve (e.g., "project", "workspace")
     * @param parameters         filter parameters for the custom resources (e.g., organization filter)
     * @param pageable           pagination information
     * @return a paginated list of provider-specific custom resources
     */
    default Page<ProviderCustomResource> getProviderCustomResources(String customResourceType, MultiValueMap<String, String> parameters, Pageable pageable) {
        throw new BadRequestException("Unsupported retrieval for resource type: " + customResourceType + " from: " + this.getClass().getSimpleName());
    }
}

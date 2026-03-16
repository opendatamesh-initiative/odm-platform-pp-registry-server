package org.opendatamesh.platform.pp.registry.utils.git.provider;

import org.opendatamesh.platform.pp.registry.utils.git.git.GitOperation;
import org.opendatamesh.platform.pp.registry.utils.git.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

/**
 * Abstraction over Git hosting providers (e.g. GitHub, GitLab, Azure DevOps, Bitbucket).
 * Defines operations for users, organizations, repositories, commits, branches, and tags,
 * plus optional provider-specific custom resources and low-level Git operations.
 */
public interface GitProvider {

    /**
     * Returns the user associated with the current provider credentials.
     *
     * @return the current authenticated user
     */
    User getCurrentUser();

    /**
     * Returns a paginated list of organizations accessible with the current credentials.
     *
     * @param page pagination and sort options
     * @return a page of organizations
     */
    Page<Organization> listOrganizations(Pageable page);

    /**
     * Looks up an organization by its provider-specific identifier.
     *
     * @param id the organization ID
     * @return the organization if found, otherwise {@link Optional#empty()}
     */
    Optional<Organization> getOrganization(String id);

    /**
     * Returns a paginated list of members of the given organization.
     *
     * @param org  the organization whose members to list
     * @param page pagination and sort options
     * @return a page of users
     */
    Page<User> listMembers(Organization org, Pageable page);

    /**
     * Returns a paginated list of repositories, either for an organization or for a user.
     * Use {@code org} for org repos; use {@code usr} (and typically {@code org == null}) for user repos.
     *
     * @param org        the organization, or {@code null} when listing user repositories
     * @param usr        the user (used when listing user repositories or as context)
     * @param parameters optional query parameters (e.g. project/workspace filter), provider-dependent
     * @param page       pagination and sort options
     * @return a page of repositories
     */
    Page<Repository> listRepositories(Organization org, User usr, MultiValueMap<String, String> parameters, Pageable page);

    /**
     * Looks up a repository by its provider-specific ID and optional owner context.
     *
     * @param id      the repository ID
     * @param ownerId the owner ID (required by some providers, may be {@code null} for others)
     * @return the repository if found, otherwise {@link Optional#empty()}
     */
    Optional<Repository> getRepository(String id, String ownerId);

    /**
     * Creates a new repository with the given metadata (name, visibility, etc.).
     *
     * @param repositoryToCreate repository definition (name and provider-specific options)
     * @return the created repository as returned by the provider
     */
    Repository createRepository(Repository repositoryToCreate);

    /**
     * Returns a paginated list of commits in the given repository, optionally filtered.
     *
     * @param repository the repository
     * @param filters    optional filters (e.g. branch, path, author); may be {@code null}
     * @param pageable   pagination and sort options
     * @return a page of commits
     */
    Page<Commit> listCommits(Repository repository, CommitListFilter filters, Pageable pageable);

    /**
     * Returns a paginated list of branches in the given repository.
     *
     * @param repository the repository
     * @param pageable   pagination and sort options
     * @return a page of branches
     */
    Page<Branch> listBranches(Repository repository, Pageable pageable);

    /**
     * Returns a paginated list of tags in the given repository.
     *
     * @param repository the repository
     * @param pageable   pagination and sort options
     * @return a page of tags
     */
    Page<Tag> listTags(Repository repository, Pageable pageable);

    /**
     * Returns an SDK for low-level Git operations (clone, read file, etc.) for this provider.
     * The implementation is provider-specific but exposes a common {@link GitOperation} API.
     *
     * @return the Git operation facade for this provider
     */
    GitOperation gitOperation();

    /**
     * Returns provider-specific custom resources that are not part of the standard Git model
     * (e.g. Bitbucket projects, GitHub projects, GitLab groups). Default implementation
     * returns an empty page; providers override to expose their custom resource types.
     *
     * @param customResourceType the type of custom resource (e.g. {@code "project"}, {@code "workspace"})
     * @param parameters         optional filter parameters (e.g. organization); provider-dependent
     * @param pageable           pagination and sort options
     * @return a paginated list of provider-specific custom resources
     */
    default Page<ProviderCustomResource> getProviderCustomResources(
            String customResourceType,
            MultiValueMap<String, String> parameters,
            Pageable pageable) {
        return Page.empty();
    }
}

package org.opendatamesh.platform.pp.registry.githandler.provider.github;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.model.filters.ListCommitFilters;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.comparator.GitHubCommitComparator;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.checkconnection.GitHubCheckConnectionUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.createrepository.GitHubCreateRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.createrepository.GitHubCreateRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.createrepository.GitHubCreateRepositoryReq;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getcurrentuser.GitHubGetCurrentUserMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getcurrentuser.GitHubGetCurrentUserUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getorganization.GitHubGetOrganizationMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getorganization.GitHubGetOrganizationOrganizationRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getrepository.GitHubGetRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getrepository.GitHubGetRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listbranches.GitHubListBranchesBranchRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listbranches.GitHubListBranchesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits.GitHubCompareCommitsRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits.GitHubListCommitsCommitRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits.GitHubListCommitsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listmembers.GitHubListMembersMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listmembers.GitHubListMembersUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listorganizations.GitHubListOrganizationsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listorganizations.GitHubListOrganizationsOrganizationRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listrepositories.GitHubListRepositoriesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listrepositories.GitHubListRepositoriesRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listtags.GitHubListTagsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listtags.GitHubListTagsTagRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * GitHub provider implementation
 * <p>
 * Supported authentication methods:
 * - OAuth 2.0
 * - PAT as Bearer token - ✅ Recommended
 *
 * <p>
 * GitHub API Limitations:
 * - listOrganizations() uses /user/orgs endpoint which returns limited organization information
 * (may not include html_url and other detailed fields)
 * - getOrganization(String id) uses /orgs/{id} endpoint which provides complete organization details
 * - For complete organization information, use getOrganization() after listing organizations
 */
public class GitHubProvider implements GitProvider {

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final GitProviderCredential credential;

    public GitHubProvider(String baseUrl, RestTemplate restTemplate, GitProviderCredential credential) throws BadRequestException {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl is required and cannot be null or empty");
        }
        this.baseUrl = baseUrl.trim();
        this.restTemplate = restTemplate;
        this.credential = credential;
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the /user endpoint to verify authentication
            ResponseEntity<GitHubCheckConnectionUserRes> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    GitHubCheckConnectionUserRes.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new GitProviderAuthenticationException("Failed to authenticate with GitHub API");
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to check connection: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to check connection: " + e.getMessage());
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitHubGetCurrentUserUserRes> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    GitHubGetCurrentUserUserRes.class
            );

            GitHubGetCurrentUserUserRes userResponse = response.getBody();
            if (userResponse != null) {
                return GitHubGetCurrentUserMapper.toInternalModel(userResponse);
            }

            throw new ClientException(404, "Failed to get current user: response body is null");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to get current user: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to get current user: " + e.getMessage());
        }
    }

    @Override
    public Page<Organization> listOrganizations(Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GitHub API Limitation: The /user/orgs endpoint returns limited organization information
            // It only provides basic fields (id, login) and may not include html_url or other detailed fields
            // For complete organization details, use getOrganization(String id) method instead
            String uriTemplate = baseUrl + "/user/orgs?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            ResponseEntity<GitHubListOrganizationsOrganizationRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitHubListOrganizationsOrganizationRes[].class,
                    uriVariables
            );

            List<Organization> organizations = new ArrayList<>();
            GitHubListOrganizationsOrganizationRes[] orgResponses = response.getBody();
            if (orgResponses != null) {
                for (GitHubListOrganizationsOrganizationRes orgResponse : orgResponses) {
                    organizations.add(GitHubListOrganizationsMapper.toInternalModel(orgResponse));
                }
            }

            return new PageImpl<>(organizations, page, organizations.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to list organizations: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to list organizations: " + e.getMessage());
        }
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GitHub API: The /orgs/{id} endpoint provides complete organization details
            // This includes all available fields like html_url, description, etc.
            // This is the recommended endpoint when you need full organization information
            String uriTemplate = baseUrl + "/orgs/{id}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("id", id);

            ResponseEntity<GitHubGetOrganizationOrganizationRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitHubGetOrganizationOrganizationRes.class,
                    uriVariables
            );

            GitHubGetOrganizationOrganizationRes orgResponse = response.getBody();
            if (orgResponse != null) {
                Organization org = GitHubGetOrganizationMapper.toInternalModel(orgResponse);
                return Optional.of(org);
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to get organization: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to get organization: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/orgs/{orgName}/members?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("orgName", org.getName());
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            ResponseEntity<GitHubListMembersUserRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitHubListMembersUserRes[].class,
                    uriVariables
            );

            List<User> members = new ArrayList<>();
            GitHubListMembersUserRes[] userResponses = response.getBody();
            if (userResponses != null) {
                for (GitHubListMembersUserRes userResponse : userResponses) {
                    User user = GitHubListMembersMapper.toInternalModel(userResponse);
                    if (user != null) {
                        members.add(user);
                    }
                }
            }

            return new PageImpl<>(members, page, members.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to list organization members: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to list organization members: " + e.getMessage());
        }
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, MultiValueMap<String, String> parameters, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate;
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            if (org != null) {
                uriTemplate = baseUrl + "/orgs/{orgName}/repos?page={page}&per_page={perPage}";
                uriVariables.put("orgName", org.getName());
            } else {
                // Use /user/repos to get ALL repositories (public + private) for authenticated user
                // /users/{username}/repos only returns public repositories
                uriTemplate = baseUrl + "/user/repos?page={page}&per_page={perPage}";
            }

            ResponseEntity<GitHubListRepositoriesRepositoryRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitHubListRepositoriesRepositoryRes[].class,
                    uriVariables
            );

            List<Repository> repositories = new ArrayList<>();
            GitHubListRepositoriesRepositoryRes[] repoResponses = response.getBody();
            if (repoResponses != null) {
                for (GitHubListRepositoriesRepositoryRes repoResponse : repoResponses) {
                    Repository repo = GitHubListRepositoriesMapper.toInternalModel(repoResponse);
                    if (repo != null) {
                        repositories.add(repo);
                    }
                }
            }

            return new PageImpl<>(repositories, page, repositories.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to list repositories: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to list repositories: " + e.getMessage());
        }
    }

    @Override
    public Optional<Repository> getRepository(String id, String ownerId) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/repositories/{id}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("id", id);

            ResponseEntity<GitHubGetRepositoryRepositoryRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitHubGetRepositoryRepositoryRes.class,
                    uriVariables
            );

            GitHubGetRepositoryRepositoryRes repoResponse = response.getBody();
            if (repoResponse != null) {
                Repository repo = GitHubGetRepositoryMapper.toInternalModel(repoResponse);
                if (repo != null) {
                    return Optional.of(repo);
                }
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to get repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to get repository: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Repository createRepository(Repository repositoryToCreate) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            headers.set("Content-Type", "application/json");

            // Create request payload
            GitHubCreateRepositoryReq request = GitHubCreateRepositoryMapper.fromInternalModel(repositoryToCreate);

            HttpEntity<GitHubCreateRepositoryReq> entity = new HttpEntity<>(request, headers);

            // Determine the correct endpoint based on owner type
            String uriTemplate;
            Map<String, Object> uriVariables = new HashMap<>();

            if (repositoryToCreate.getOwnerType() == OwnerType.ORGANIZATION && repositoryToCreate.getOwnerId() != null) {
                // Create repository under organization
                uriTemplate = baseUrl + "/orgs/{ownerId}/repos";
                uriVariables.put("ownerId", repositoryToCreate.getOwnerId());
            } else {
                // Create repository under authenticated user
                uriTemplate = baseUrl + "/user/repos";
            }

            ResponseEntity<GitHubCreateRepositoryRepositoryRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.POST,
                    entity,
                    GitHubCreateRepositoryRepositoryRes.class,
                    uriVariables
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GitHubCreateRepositoryRepositoryRes repoResponse = response.getBody();
                return GitHubCreateRepositoryMapper.toInternalModel(repoResponse);
            }

            throw new ClientException(response.getStatusCode().value(), "Failed to create repository. Status: " + response.getStatusCode());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to create repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to create repository: " + e.getMessage());
        }
    }

    @Override
    public Page<Commit> listCommits(Repository repository, ListCommitFilters commitFilters, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GET /repos/{owner}/{repo}/commits
            // cannot use IDs directly
            String ownerName = getOwnerName(repository);
            String repoName = repository.getName();


            if (existsAndNotEmptyFromAndToCommitFilters(commitFilters)) {
                String uriTemplate = baseUrl + "/repos/{owner}/{repo}/compare/{from}...{to}?page={page}&per_page={perPage}";

                Optional<FromAndToCommitFilters> fromAndToFiltersResolved = resolveFromAndToCommitFilters(commitFilters);

                Map<String, Object> uriVariables = constructUriVariablesListCommitsCompare(ownerName, repoName, fromAndToFiltersResolved.get(), page);

                ResponseEntity<GitHubCompareCommitsRes> response = callApiListCommitsCompare(uriTemplate, entity, uriVariables);

                List<Commit> commits = mappingListCommitsCompareToInternalModel(response);

                // Sort in reverse chronological order because GitHub API /compare returns in chronological order
                commits.sort(new GitHubCommitComparator());

                return new PageImpl<>(commits, page, commits.size());
            } else {
                String uriTemplate = baseUrl + "/repos/{owner}/{repo}/commits?page={page}&per_page={perPage}";

                Map<String, Object> uriVariables = constructUriVariablesListCommits(ownerName, repoName, page);

                ResponseEntity<GitHubListCommitsCommitRes[]> response = callApiListCommits(uriTemplate, entity, uriVariables);

                List<Commit> commits = mappingListCommitsToInternalModel(response);

                return new PageImpl<>(commits, page, commits.size());
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to list commits: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to list commits: " + e.getMessage());
        }
    }

    @Override
    public Page<Branch> listBranches(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GET /repos/{owner}/{repo}/branches
            // cannot use IDs directly
            String ownerName = getOwnerName(repository);
            String repoName = repository.getName();
            String uriTemplate = baseUrl + "/repos/{owner}/{repo}/branches?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("owner", ownerName);
            uriVariables.put("repo", repoName);
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            ResponseEntity<GitHubListBranchesBranchRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitHubListBranchesBranchRes[].class,
                    uriVariables
            );

            List<Branch> branches = new ArrayList<>();
            GitHubListBranchesBranchRes[] branchResponses = response.getBody();
            if (branchResponses != null) {
                for (GitHubListBranchesBranchRes branchResponse : branchResponses) {
                    Branch branch = GitHubListBranchesMapper.toInternalModel(branchResponse);
                    if (branch != null) {
                        branches.add(branch);
                    }
                }
            }

            return new PageImpl<>(branches, page, branches.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to list branches: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to list branches: " + e.getMessage());
        }
    }

    @Override
    public Page<Tag> listTags(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GET /repos/{owner}/{repo}/tags
            // cannot use IDs directly
            String ownerName = getOwnerName(repository);
            String repoName = repository.getName();
            String uriTemplate = baseUrl + "/repos/{owner}/{repo}/tags?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("owner", ownerName);
            uriVariables.put("repo", repoName);
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            ResponseEntity<GitHubListTagsTagRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitHubListTagsTagRes[].class,
                    uriVariables
            );

            List<Tag> tags = new ArrayList<>();
            GitHubListTagsTagRes[] tagResponses = response.getBody();
            if (tagResponses != null) {
                for (GitHubListTagsTagRes tagResponse : tagResponses) {
                    Tag tag = GitHubListTagsMapper.toInternalModel(tagResponse);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
            }

            return new PageImpl<>(tags, page, tags.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitHub authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitHub request failed to list tags: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitHub request failed to list tags: " + e.getMessage());
        }
    }


    /**
     * Creates a GitAuthContext based on the available credentials in this provider
     *
     * @return configured GitAuthContext
     */
    public GitAuthContext createGitAuthContext() {
        return credential.createGitAuthContext();
    }

    private String getOwnerName(Repository repository) {
        if (repository.getOwnerType() == OwnerType.ORGANIZATION) {
            return getOrganization(repository.getOwnerId()).get().getName();
        } else {
            return getCurrentUser().getUsername();
        }
    }

    public record FromAndToCommitFilters(String from, String to) {
    }

    private String extractFromCommitFilter(ListCommitFilters commitFilters) {
        if (StringUtils.hasText(commitFilters.fromTagName())) {
            return commitFilters.fromTagName();
        }
        if (StringUtils.hasText(commitFilters.fromCommitHash())) {
            return commitFilters.fromCommitHash();
        }
        return commitFilters.fromBranchName();
    }

    private String extractToCommitFilter(ListCommitFilters commitFilters) {
        if (StringUtils.hasText(commitFilters.toTagName())) {
            return commitFilters.toTagName();
        }
        if (StringUtils.hasText(commitFilters.toCommitHash())) {
            return commitFilters.toCommitHash();
        }
        return commitFilters.toBranchName();
    }

    private Optional<FromAndToCommitFilters> resolveFromAndToCommitFilters(ListCommitFilters commitFilters) {
        if (commitFilters != null) {
            String from = extractFromCommitFilter(commitFilters);
            String to = extractToCommitFilter(commitFilters);

            // Validate: if one is specified, both must be specified
            boolean fromSpecified = StringUtils.hasText(from);
            boolean toSpecified = StringUtils.hasText(to);

            if (fromSpecified != toSpecified) {
                throw new BadRequestException("For GitHub provider from and to parameters are mandatory");
            }

            if (fromSpecified && toSpecified) {
                return Optional.of(new FromAndToCommitFilters(from, to));
            }
        }
        return Optional.empty();
    }

    private boolean existsAndNotEmptyFromAndToCommitFilters(ListCommitFilters commitFilters) {
        Optional<FromAndToCommitFilters> refPair = resolveFromAndToCommitFilters(commitFilters);
        return refPair.isPresent();
    }

    private ResponseEntity<GitHubCompareCommitsRes> callApiListCommitsCompare(String uriTemplate, HttpEntity<String> entity, Map<String, Object> uriVariables) {
        return restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                GitHubCompareCommitsRes.class,
                uriVariables
        );
    }

    private ResponseEntity<GitHubListCommitsCommitRes[]> callApiListCommits(String uriTemplate, HttpEntity<String> entity, Map<String, Object> uriVariables) {
        return restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                GitHubListCommitsCommitRes[].class,
                uriVariables
        );
    }

    private Map<String, Object> constructUriVariablesListCommitsCompare(String ownerName, String repoName, FromAndToCommitFilters fromAndToFilters, Pageable page) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("owner", ownerName);
        uriVariables.put("repo", repoName);
        uriVariables.put("from", fromAndToFilters.from);
        uriVariables.put("to", fromAndToFilters.to);
        uriVariables.put("page", page.getPageNumber() + 1);
        uriVariables.put("perPage", page.getPageSize());
        return uriVariables;
    }

    private Map<String, Object> constructUriVariablesListCommits(String ownerName, String repoName, Pageable page) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("owner", ownerName);
        uriVariables.put("repo", repoName);
        uriVariables.put("page", page.getPageNumber() + 1);
        uriVariables.put("perPage", page.getPageSize());
        return uriVariables;
    }

    private List<Commit> mappingListCommitsCompareToInternalModel(ResponseEntity<GitHubCompareCommitsRes> response) {
        List<Commit> commits = new ArrayList<>();
        GitHubCompareCommitsRes compareResponses = response.getBody();
        if (compareResponses != null && compareResponses.getCommits() != null) {
            for (GitHubCompareCommitsRes.CompareCommitRes compareResponse : compareResponses.getCommits()) {
                Commit commit = GitHubListCommitsMapper.toInternalModel(compareResponse);
                if (commit != null) {
                    commits.add(commit);
                }
            }
        }
        return commits;
    }

    private List<Commit> mappingListCommitsToInternalModel(ResponseEntity<GitHubListCommitsCommitRes[]> response) {
        List<Commit> commits = new ArrayList<>();
        GitHubListCommitsCommitRes[] commitResponses = response.getBody();
        if (commitResponses != null) {
            for (GitHubListCommitsCommitRes commitResponse : commitResponses) {
                Commit commit = GitHubListCommitsMapper.toInternalModel(commitResponse);
                if (commit != null) {
                    commits.add(commit);
                }
            }
        }
        return commits;
    }
}
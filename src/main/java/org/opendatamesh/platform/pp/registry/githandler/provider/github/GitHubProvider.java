package org.opendatamesh.platform.pp.registry.githandler.provider.github;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.model.filters.ListCommitFilters;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
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
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.github.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
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
            Optional<RefPair> refPairOpt = resolveCompareRefs(commitFilters);

            List<Commit> commits = refPairOpt
                    .map(refPair -> fetchCommitsWithCompareRefs(refPair, page, headers, ownerName, repoName))
                    .orElseGet(() -> fetchCommitsWithoutCompareRefs(page, headers, ownerName, repoName));

            return new PageImpl<>(commits, page, commits.size());
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

    private List<Commit> fetchCommitsWithCompareRefs(RefPair refs, Pageable page, HttpHeaders headers, String ownerName, String repoName) {
        String uriTemplate = buildCompareCommitsUriTemplate();
        Map<String, Object> uriVariables = buildCompareCommitsUriVariables(ownerName, repoName, refs, page);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GitHubCompareCommitsRes> response = restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                GitHubCompareCommitsRes.class,
                uriVariables
        );

        return parseCompareCommitsResponse(response.getBody());
    }

    private List<Commit> fetchCommitsWithoutCompareRefs(Pageable page, HttpHeaders headers, String ownerName, String repoName) {
        String uriTemplate = buildCommitsUriTemplate();
        Map<String, Object> uriVariables = buildCommitsUriVariables(ownerName, repoName, page);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GitHubListCommitsCommitRes[]> response = restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                GitHubListCommitsCommitRes[].class,
                uriVariables
        );

        return parseCommitsResponse(response.getBody());
    }

    private Map<String, Object> buildCompareCommitsUriVariables(String ownerName, String repoName, RefPair refs, Pageable page) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("owner", ownerName);
        uriVariables.put("repo", repoName);
        uriVariables.put("from", refs.from());
        uriVariables.put("to", refs.to());
        uriVariables.put("page", page.getPageNumber() + 1);
        uriVariables.put("perPage", page.getPageSize());
        return uriVariables;
    }

    private Map<String, Object> buildCommitsUriVariables(String ownerName, String repoName, Pageable page) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("owner", ownerName);
        uriVariables.put("repo", repoName);
        uriVariables.put("page", page.getPageNumber() + 1);
        uriVariables.put("perPage", page.getPageSize());
        return uriVariables;
    }

    private List<Commit> parseCompareCommitsResponse(GitHubCompareCommitsRes compareResponses) {
        List<Commit> commits = new ArrayList<>();
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

    private List<Commit> parseCommitsResponse(GitHubListCommitsCommitRes[] commitResponses) {
        List<Commit> commits = new ArrayList<>();
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

    private String buildCompareCommitsUriTemplate() {
        return baseUrl + "/repos/{owner}/{repo}/compare/{from}...{to}?page={page}&per_page={perPage}";
    }

    private String buildCommitsUriTemplate() {
        return baseUrl + "/repos/{owner}/{repo}/commits?page={page}&per_page={perPage}";
    }

    public record RefPair(String from, String to) {}

    private Optional<RefPair> resolveCompareRefs(ListCommitFilters commitFilters) {

        if (commitFilters.fromTagName() != null && commitFilters.toTagName() != null) {
            return Optional.of(new RefPair(commitFilters.fromTagName(), commitFilters.toTagName()));
        }

        if (commitFilters.fromCommitHash() != null && commitFilters.toCommitHash() != null) {
            return Optional.of(new RefPair(commitFilters.fromCommitHash(), commitFilters.toCommitHash()));
        }

        if (commitFilters.fromBranchName() != null && commitFilters.toBranchName() != null) {
            return Optional.of(new RefPair(commitFilters.fromBranchName(), commitFilters.toBranchName()));
        }

        return Optional.empty();
    }
}
package org.opendatamesh.platform.pp.registry.githandler.provider.github;

import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final Credential credential;

    public GitHubProvider(String baseUrl, RestTemplate restTemplate, Credential credential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.github.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.credential = credential;
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the /user endpoint to verify authentication
            ResponseEntity<GitHubCheckConnectionUserRes> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    GitHubCheckConnectionUserRes.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access the API with our credentials
                return;
            } else {
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
            HttpHeaders headers = createGitHubHeaders();
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GitHub API Limitation: The /user/orgs endpoint returns limited organization information
            // It only provides basic fields (id, login) and may not include html_url or other detailed fields
            // For complete organization details, use getOrganization(String id) method instead
            String url = baseUrl + "/user/orgs?page=" + (page.getPageNumber() + 1) +
                    "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubListOrganizationsOrganizationRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubListOrganizationsOrganizationRes[].class
            );

            List<Organization> organizations = new ArrayList<>();
            GitHubListOrganizationsOrganizationRes[] orgResponses = response.getBody();
            if (orgResponses != null) {
                for (GitHubListOrganizationsOrganizationRes orgResponse : orgResponses) {
                    Organization org = GitHubListOrganizationsMapper.toInternalModel(orgResponse);
                    if (org != null) {
                        organizations.add(org);
                    }
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GitHub API: The /orgs/{id} endpoint provides complete organization details
            // This includes all available fields like html_url, description, etc.
            // This is the recommended endpoint when you need full organization information
            ResponseEntity<GitHubGetOrganizationOrganizationRes> response = restTemplate.exchange(
                    baseUrl + "/orgs/" + id,
                    HttpMethod.GET,
                    entity,
                    GitHubGetOrganizationOrganizationRes.class
            );

            GitHubGetOrganizationOrganizationRes orgResponse = response.getBody();
            if (orgResponse != null) {
                Organization org = GitHubGetOrganizationMapper.toInternalModel(orgResponse);
                if (org != null) {
                    return Optional.of(org);
                }
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/orgs/" + org.getName() + "/members?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubListMembersUserRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubListMembersUserRes[].class
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url;
            if (org != null) {
                url = baseUrl + "/orgs/" + org.getName() + "/repos?page=" +
                        (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();
            } else {
                // Use /user/repos to get ALL repositories (public + private) for authenticated user
                // /users/{username}/repos only returns public repositories
                url = baseUrl + "/user/repos?page=" +
                        (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();
            }

            ResponseEntity<GitHubListRepositoriesRepositoryRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubListRepositoriesRepositoryRes[].class
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
    public Optional<Repository> getRepository(String id) {
        try {
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitHubGetRepositoryRepositoryRes> response = restTemplate.exchange(
                    baseUrl + "/repositories/" + id,
                    HttpMethod.GET,
                    entity,
                    GitHubGetRepositoryRepositoryRes.class
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
            HttpHeaders headers = createGitHubHeaders();
            headers.set("Content-Type", "application/json");

            // Create request payload
            GitHubCreateRepositoryReq request = GitHubCreateRepositoryMapper.fromInternalModel(repositoryToCreate);

            HttpEntity<GitHubCreateRepositoryReq> entity = new HttpEntity<>(request, headers);

            // Determine the correct endpoint based on owner type
            String endpoint;
            if (repositoryToCreate.getOwnerType() == OwnerType.ORGANIZATION && repositoryToCreate.getOwnerId() != null) {
                // Create repository under organization
                endpoint = baseUrl + "/orgs/" + repositoryToCreate.getOwnerId() + "/repos";
            } else {
                // Create repository under authenticated user
                endpoint = baseUrl + "/user/repos";
            }

            ResponseEntity<GitHubCreateRepositoryRepositoryRes> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    GitHubCreateRepositoryRepositoryRes.class
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
    public Page<Commit> listCommits(Organization org, User usr, Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Determine owner from org or user
            String owner = (org != null) ? org.getName() : usr.getUsername();
            String repoName = repository.getName();

            // URL encode the owner and repoName to handle special characters
            String encodedOwner = URLEncoder.encode(owner, StandardCharsets.UTF_8);
            String encodedRepoName = URLEncoder.encode(repoName, StandardCharsets.UTF_8);

            String url = baseUrl + "/repos/" + encodedOwner + "/" + encodedRepoName + "/commits?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubListCommitsCommitRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubListCommitsCommitRes[].class
            );

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
    public Page<Branch> listBranches(Organization org, User usr, Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Determine owner from org or user
            String owner = (org != null) ? org.getName() : usr.getUsername();
            String repoName = repository.getName();

            // URL encode the owner and repoName to handle special characters
            String encodedOwner = URLEncoder.encode(owner, StandardCharsets.UTF_8);
            String encodedRepoName = URLEncoder.encode(repoName, StandardCharsets.UTF_8);

            String url = baseUrl + "/repos/" + encodedOwner + "/" + encodedRepoName + "/branches?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubListBranchesBranchRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubListBranchesBranchRes[].class
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
    public Page<Tag> listTags(Organization org, User usr, Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Determine owner from org or user
            String owner = (org != null) ? org.getName() : usr.getUsername();
            String repoName = repository.getName();

            // URL encode the owner and repoName to handle special characters
            String encodedOwner = URLEncoder.encode(owner, StandardCharsets.UTF_8);
            String encodedRepoName = URLEncoder.encode(repoName, StandardCharsets.UTF_8);

            String url = baseUrl + "/repos/" + encodedOwner + "/" + encodedRepoName + "/tags?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubListTagsTagRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubListTagsTagRes[].class
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
     * Create GitHub-specific HTTP headers for authentication.
     * Supports both Bearer token and Basic authentication.
     */
    private HttpHeaders createGitHubHeaders() {
        if (this.credential instanceof PatCredential pat) return createGitHubHeaders(pat);
        throw new IllegalArgumentException("Unknown credential type");
    }

    private HttpHeaders createGitHubHeaders(PatCredential credential) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(credential.getToken());

        // Add common headers for GitHub API
        headers.set("Accept", "application/vnd.github.v3+json");//TODO
        headers.set("User-Agent", "GitProviderDemo/1.0");//TODO

        return headers;
    }

    /**
     * Creates a GitAuthContext based on the available credentials in this provider
     *
     * @return configured GitAuthContext
     */
    public GitAuthContext createGitAuthContext() {
        if (this.credential instanceof PatCredential pat) return createGitAuthContext(pat);
        throw new UnsupportedOperationException("Unknown credential type");
    }

    private GitAuthContext createGitAuthContext(PatCredential credential) {
        GitAuthContext ctx = new GitAuthContext();
        ctx.transportProtocol = GitAuthContext.TransportProtocol.HTTP;
        if (credential != null && credential.getToken() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", credential.getToken());
            ctx.httpAuthHeaders = headers;
        }
        return ctx;
    }
}

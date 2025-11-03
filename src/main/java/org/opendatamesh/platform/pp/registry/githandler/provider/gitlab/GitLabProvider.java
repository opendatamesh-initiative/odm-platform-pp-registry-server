package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab;

import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.checkconnection.GitLabCheckConnectionUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.createrepository.GitLabCreateRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.createrepository.GitLabCreateRepositoryProjectRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.createrepository.GitLabCreateRepositoryReq;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getcurrentuser.GitLabGetCurrentUserMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getcurrentuser.GitLabGetCurrentUserUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getorganization.GitLabGetOrganizationGroupRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getorganization.GitLabGetOrganizationMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getrepository.GitLabGetRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getrepository.GitLabGetRepositoryProjectRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listbranches.GitLabListBranchesBranchRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listbranches.GitLabListBranchesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listcommits.GitLabListCommitsCommitRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listcommits.GitLabListCommitsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listmembers.GitLabListMembersMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listmembers.GitLabListMembersUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listorganizations.GitLabListOrganizationsGroupRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listorganizations.GitLabListOrganizationsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listrepositories.GitLabListRepositoriesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listrepositories.GitLabListRepositoriesProjectRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listtags.GitLabListTagsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listtags.GitLabListTagsTagRes;
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
 * GitLab provider implementation
 * <p>
 * Supported authentication methods:
 * - OAuth 2.0 (Bearer token) - ✅ Recommended
 * - PAT as Bearer token - ✅ Recommended
 */
public class GitLabProvider implements GitProvider {

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final Credential credential;

    public GitLabProvider(String baseUrl, RestTemplate restTemplate, Credential credential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://gitlab.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.credential = credential;
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the /user endpoint to verify authentication
            ResponseEntity<GitLabCheckConnectionUserRes> response = restTemplate.exchange(
                    baseUrl + "/api/v4/user",
                    HttpMethod.GET,
                    entity,
                    GitLabCheckConnectionUserRes.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access GitLab with our credentials
                return;
            } else {
                throw new GitProviderAuthenticationException("Failed to authenticate with GitLab API");
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to check connection: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to check connection: " + e.getMessage());
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitLabGetCurrentUserUserRes> response = restTemplate.exchange(
                    baseUrl + "/api/v4/user",
                    HttpMethod.GET,
                    entity,
                    GitLabGetCurrentUserUserRes.class
            );

            GitLabGetCurrentUserUserRes userResponse = response.getBody();
            if (userResponse != null) {
                return GitLabGetCurrentUserMapper.toInternalModel(userResponse);
            }

            throw new ClientException(404, "Failed to get current user: response body is null");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to get current user: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to get current user: " + e.getMessage());
        }
    }

    @Override
    public Page<Organization> listOrganizations(Pageable page) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/api/v4/groups?page=" + (page.getPageNumber() + 1) +
                    "&per_page=" + page.getPageSize() + "&owned=true";

            ResponseEntity<GitLabListOrganizationsGroupRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabListOrganizationsGroupRes[].class
            );

            List<Organization> organizations = new ArrayList<>();
            GitLabListOrganizationsGroupRes[] groupResponses = response.getBody();
            if (groupResponses != null) {
                for (GitLabListOrganizationsGroupRes groupResponse : groupResponses) {
                    Organization org = GitLabListOrganizationsMapper.toInternalModel(groupResponse);
                    if (org != null) {
                        organizations.add(org);
                    }
                }
            }

            return new PageImpl<>(organizations, page, organizations.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to list organizations: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to list organizations: " + e.getMessage());
        }
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitLabGetOrganizationGroupRes> response = restTemplate.exchange(
                    baseUrl + "/api/v4/groups/" + id,
                    HttpMethod.GET,
                    entity,
                    GitLabGetOrganizationGroupRes.class
            );

            GitLabGetOrganizationGroupRes groupResponse = response.getBody();
            if (groupResponse != null) {
                Organization org = GitLabGetOrganizationMapper.toInternalModel(groupResponse);
                if (org != null) {
                    return Optional.of(org);
                }
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to get organization: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to get organization: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable page) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/api/v4/groups/" + org.getId() + "/members?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitLabListMembersUserRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabListMembersUserRes[].class
            );

            List<User> members = new ArrayList<>();
            GitLabListMembersUserRes[] userResponses = response.getBody();
            if (userResponses != null) {
                for (GitLabListMembersUserRes userResponse : userResponses) {
                    User user = GitLabListMembersMapper.toInternalModel(userResponse);
                    if (user != null) {
                        members.add(user);
                    }
                }
            }

            return new PageImpl<>(members, page, members.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to list organization members: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to list organization members: " + e.getMessage());
        }
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, MultiValueMap<String, String> parameters, Pageable page) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url;
            if (org != null) {
                url = baseUrl + "/api/v4/groups/" + org.getId() + "/projects?page=" +
                        (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();
            } else {
                url = baseUrl + "/api/v4/users/" + usr.getId() + "/projects?page=" +
                        (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();
            }

            ResponseEntity<GitLabListRepositoriesProjectRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabListRepositoriesProjectRes[].class
            );

            List<Repository> repositories = new ArrayList<>();
            GitLabListRepositoriesProjectRes[] projectResponses = response.getBody();
            if (projectResponses != null) {
                OwnerType ownerType = org != null ? OwnerType.ORGANIZATION : OwnerType.ACCOUNT;
                for (GitLabListRepositoriesProjectRes projectResponse : projectResponses) {
                    Repository repo = GitLabListRepositoriesMapper.toInternalModel(projectResponse, ownerType);
                    if (repo != null) {
                        repositories.add(repo);
                    }
                }
            }

            return new PageImpl<>(repositories, page, repositories.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to list repositories: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to list repositories: " + e.getMessage());
        }
    }

    @Override
    public Optional<Repository> getRepository(String id) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitLabGetRepositoryProjectRes> response = restTemplate.exchange(
                    baseUrl + "/api/v4/projects/" + id,
                    HttpMethod.GET,
                    entity,
                    GitLabGetRepositoryProjectRes.class
            );

            GitLabGetRepositoryProjectRes projectResponse = response.getBody();
            if (projectResponse != null) {
                Repository repo = GitLabGetRepositoryMapper.toInternalModel(projectResponse);
                if (repo != null) {
                    return Optional.of(repo);
                }
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to get repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to get repository: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Repository createRepository(Repository repositoryToCreate) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            headers.set("Content-Type", "application/json");

            // Create request payload
            GitLabCreateRepositoryReq request = GitLabCreateRepositoryMapper.fromInternalModel(repositoryToCreate);

            HttpEntity<GitLabCreateRepositoryReq> entity = new HttpEntity<>(request, headers);

            ResponseEntity<GitLabCreateRepositoryProjectRes> response = restTemplate.exchange(
                    baseUrl + "/api/v4/projects",
                    HttpMethod.POST,
                    entity,
                    GitLabCreateRepositoryProjectRes.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GitLabCreateRepositoryProjectRes projectResponse = response.getBody();
                return GitLabCreateRepositoryMapper.toInternalModel(projectResponse, repositoryToCreate.getOwnerType());
            }

            throw new ClientException(response.getStatusCode().value(), "Failed to create repository. Status: " + response.getStatusCode());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to create repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to create repository: " + e.getMessage());
        }
    }

    @Override
    public Page<Commit> listCommits(Organization org, User usr, Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Extract project ID from repository
            String projectId = repository.getId();
            
            // URL encode the projectId to handle special characters
            String encodedProjectId = URLEncoder.encode(projectId, StandardCharsets.UTF_8);
            
            String url = baseUrl + "/api/v4/projects/" + encodedProjectId + "/repository/commits?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitLabListCommitsCommitRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabListCommitsCommitRes[].class
            );

            List<Commit> commits = new ArrayList<>();
            GitLabListCommitsCommitRes[] commitResponses = response.getBody();
            if (commitResponses != null) {
                for (GitLabListCommitsCommitRes commitResponse : commitResponses) {
                    Commit commit = GitLabListCommitsMapper.toInternalModel(commitResponse);
                    if (commit != null) {
                        commits.add(commit);
                    }
                }
            }

            return new PageImpl<>(commits, page, commits.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to list commits: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to list commits: " + e.getMessage());
        }
    }

    @Override
    public Page<Branch> listBranches(Organization org, User usr, Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Extract project ID from repository
            String projectId = repository.getId();
            
            // URL encode the projectId to handle special characters
            String encodedProjectId = URLEncoder.encode(projectId, StandardCharsets.UTF_8);
            
            String url = baseUrl + "/api/v4/projects/" + encodedProjectId + "/repository/branches?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitLabListBranchesBranchRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabListBranchesBranchRes[].class
            );

            List<Branch> branches = new ArrayList<>();
            GitLabListBranchesBranchRes[] branchResponses = response.getBody();
            if (branchResponses != null) {
                for (GitLabListBranchesBranchRes branchResponse : branchResponses) {
                    Branch branch = GitLabListBranchesMapper.toInternalModel(branchResponse);
                    if (branch != null) {
                        branches.add(branch);
                    }
                }
            }

            return new PageImpl<>(branches, page, branches.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to list branches: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to list branches: " + e.getMessage());
        }
    }

    @Override
    public Page<Tag> listTags(Organization org, User usr, Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Extract project ID from repository
            String projectId = repository.getId();
            
            // URL encode the projectId to handle special characters
            String encodedProjectId = URLEncoder.encode(projectId, StandardCharsets.UTF_8);
            
            String url = baseUrl + "/api/v4/projects/" + encodedProjectId + "/repository/tags?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitLabListTagsTagRes[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabListTagsTagRes[].class
            );

            List<Tag> tags = new ArrayList<>();
            GitLabListTagsTagRes[] tagResponses = response.getBody();
            if (tagResponses != null) {
                for (GitLabListTagsTagRes tagResponse : tagResponses) {
                    Tag tag = GitLabListTagsMapper.toInternalModel(tagResponse);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
            }

            return new PageImpl<>(tags, page, tags.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "GitLab request failed to list tags: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "GitLab request failed to list tags: " + e.getMessage());
        }
    }

    /**
     * Create GitLab-specific HTTP headers for authentication.
     * Uses Bearer token authentication with Personal Access Tokens.
     */
    private HttpHeaders createGitLabHeaders() {
        if (this.credential instanceof PatCredential pat) return createGitLabHeaders(pat);
        throw new IllegalArgumentException("Unknown credential type");
    }

    private HttpHeaders createGitLabHeaders(PatCredential credential) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(credential.getToken());

        // Add common headers for GitLab API
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "GitProviderDemo/1.0");
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
            headers.set("Authorization", credential.getToken());  // TODO: could need Bearer?
            ctx.httpAuthHeaders = headers;
        }
        return ctx;
    }
}

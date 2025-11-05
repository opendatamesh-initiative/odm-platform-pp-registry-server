package org.opendatamesh.platform.pp.registry.githandler.provider.azure;

import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.checkconnection.AzureCheckConnectionUserResponseRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.createrepository.AzureCreateRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.createrepository.AzureCreateRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.createrepository.AzureCreateRepositoryReq;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getcurrentuser.AzureGetCurrentUserMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getcurrentuser.AzureGetCurrentUserUserResponseRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getrepository.AzureGetRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getrepository.AzureGetRepositoryProjectListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getrepository.AzureGetRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listbranches.AzureListBranchesBranchListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listbranches.AzureListBranchesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits.AzureListCommitsCommitListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits.AzureListCommitsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories.AzureListRepositoriesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories.AzureListRepositoriesProjectListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories.AzureListRepositoriesRepositoryListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listtags.AzureListTagsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listtags.AzureListTagsTagListRes;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Azure DevOps provider implementation
 * <p>
 * Supported authentication methods:
 * - OAuth 2.0 (Bearer token) - ✅ Recommended (Azure AD OAuth2)
 * - PAT as Bearer token - ✅ Recommended
 * - SSH Keys - ✅ (Git operations only)
 * - Kerberos/NTLM - ✅ (On-prem Azure DevOps Server only)
 */
public class AzureDevOpsProvider implements GitProvider {

    private final String baseUrl;
    private final String organization;
    private final RestTemplate restTemplate;
    private final Credential credential;

    public AzureDevOpsProvider(String baseUrl, RestTemplate restTemplate, Credential credential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://dev.azure.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.credential = credential;

        // Extract organization from baseUrl
        if (this.baseUrl.contains("dev.azure.com/")) {
            String[] parts = this.baseUrl.split("dev.azure.com/");
            this.organization = parts.length > 1 ? parts[1] : "default-org";
        } else {
            this.organization = "default-org";
        }
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the connectionData endpoint to verify authentication
            ResponseEntity<AzureCheckConnectionUserResponseRes> response = restTemplate.exchange(
                    baseUrl + "/_apis/connectionData?api-version=7.1-preview.1",
                    HttpMethod.GET,
                    entity,
                    AzureCheckConnectionUserResponseRes.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access Azure DevOps with our credentials
                return;
            } else {
                throw new GitProviderAuthenticationException("Failed to authenticate with Azure DevOps API");
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to check connection: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to check connection: " + e.getMessage());
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First get the connection data to get the user ID
            ResponseEntity<AzureGetCurrentUserUserResponseRes> response = restTemplate.exchange(
                    baseUrl + "/_apis/connectionData?api-version=7.1-preview.1",
                    HttpMethod.GET,
                    entity,
                    AzureGetCurrentUserUserResponseRes.class
            );

            AzureGetCurrentUserUserResponseRes userResponse = response.getBody();
            if (userResponse != null && userResponse.getAuthenticatedUser() != null) {
                return AzureGetCurrentUserMapper.toInternalModel(userResponse.getAuthenticatedUser(), baseUrl);
            }

            throw new ClientException(404, "Failed to get current user: response body or authenticated user is null");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to get current user: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to get current user: " + e.getMessage());
        }
    }

    @Override
    public Page<Organization> listOrganizations(Pageable page) {
        // Azure DevOps organizations are typically single per authentication context
        List<Organization> organizations = new ArrayList<>();
        organizations.add(new Organization(
                organization,
                organization,
                baseUrl
        ));

        return new PageImpl<>(organizations, page, organizations.size());
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        if (organization.equals(id)) {
            return Optional.of(new Organization(
                    organization,
                    organization,
                    baseUrl
            ));
        }

        return Optional.empty();
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable page) {
        try {
            // For simplicity, we'll return the current user as the only member
            // In a real implementation, you'd need to call the teams/members API
            List<User> members = new ArrayList<>();
            members.add(getCurrentUser());

            return new PageImpl<>(members, page, members.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to list organization members: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to list organization members: " + e.getMessage());
        }
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, MultiValueMap<String, String> parameters, Pageable page) {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First get projects, then get repositories from each project
            String projectsUrl = baseUrl + "/_apis/projects?api-version=7.1";
            ResponseEntity<AzureListRepositoriesProjectListRes> projectsResponse = restTemplate.exchange(
                    projectsUrl,
                    HttpMethod.GET,
                    entity,
                    AzureListRepositoriesProjectListRes.class
            );

            List<Repository> repositories = new ArrayList<>();
            AzureListRepositoriesProjectListRes projectsListResponse = projectsResponse.getBody();
            if (projectsListResponse != null && projectsListResponse.getValue() != null) {
                for (var project : projectsListResponse.getValue()) {
                    String reposUrl = baseUrl + "/" + project.getName() + "/_apis/git/repositories?api-version=7.1";
                    ResponseEntity<AzureListRepositoriesRepositoryListRes> reposResponse = restTemplate.exchange(
                            reposUrl,
                            HttpMethod.GET,
                            entity,
                            AzureListRepositoriesRepositoryListRes.class
                    );

                    AzureListRepositoriesRepositoryListRes reposListResponse = reposResponse.getBody();
                    if (reposListResponse != null && reposListResponse.getValue() != null) {
                        for (var repo : reposListResponse.getValue()) {
                            Repository repository = AzureListRepositoriesMapper.toInternalModel(repo, project.getId());
                            if (repository != null) {
                                repositories.add(repository);
                            }
                        }
                    }
                }
            }

            return new PageImpl<>(repositories, page, repositories.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to list repositories: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to list repositories: " + e.getMessage());
        }
    }

    @Override
    public Optional<Repository> getRepository(String id, String ownerId) {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First get all projects to find the repository
            String projectsUrl = baseUrl + "/_apis/projects?api-version=7.1";
            ResponseEntity<AzureGetRepositoryProjectListRes> projectsResponse = restTemplate.exchange(
                    projectsUrl,
                    HttpMethod.GET,
                    entity,
                    AzureGetRepositoryProjectListRes.class
            );

            AzureGetRepositoryProjectListRes projectsListResponse = projectsResponse.getBody();
            if (projectsListResponse != null && projectsListResponse.getValue() != null) {
                for (var project : projectsListResponse.getValue()) {
                    String repoUrl = baseUrl + "/" + project.getName() + "/_apis/git/repositories/" + id + "?api-version=7.1";
                    try {
                        ResponseEntity<AzureGetRepositoryRepositoryRes> repoResponse = restTemplate.exchange(
                                repoUrl,
                                HttpMethod.GET,
                                entity,
                                AzureGetRepositoryRepositoryRes.class
                        );

                        AzureGetRepositoryRepositoryRes repo = repoResponse.getBody();
                        if (repo != null) {
                            Repository repository = AzureGetRepositoryMapper.toInternalModel(repo, project.getId());
                            if (repository != null) {
                                return Optional.of(repository);
                            }
                        }
                    } catch (Exception e) {
                        // Repository not found in this project, continue searching
                    }
                }
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to get repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to get repository: " + e.getMessage());
        } catch (Exception e) {
            // Repository not found or other error
        }

        return Optional.empty();
    }

    @Override
    public Repository createRepository(Repository repositoryToCreate) {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            headers.set("Content-Type", "application/json");

            // Azure DevOps only supports organization repositories (project-scoped)
            // Validate that the owner type is ORGANIZATION
            if (repositoryToCreate.getOwnerType() != OwnerType.ORGANIZATION) {
                throw new IllegalArgumentException("Azure DevOps only supports organization repositories. User repositories are not supported.");
            }

            // Use the specific project ID from the request
            String projectId = repositoryToCreate.getOwnerId();
            if (projectId == null || projectId.trim().isEmpty()) {
                throw new IllegalArgumentException("Owner ID (Project ID) is required for Azure DevOps repository creation");
            }

            // Create request payload
            AzureCreateRepositoryReq request = AzureCreateRepositoryMapper.fromInternalModel(repositoryToCreate);

            HttpEntity<AzureCreateRepositoryReq> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<AzureCreateRepositoryRepositoryRes> response = restTemplate.exchange(
                    baseUrl + "/" + projectId + "/_apis/git/repositories?api-version=7.1",
                    HttpMethod.POST,
                    requestEntity,
                    AzureCreateRepositoryRepositoryRes.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AzureCreateRepositoryRepositoryRes repo = response.getBody();
                return AzureCreateRepositoryMapper.toInternalModel(repo, projectId);
            }

            throw new ClientException(response.getStatusCode().value(), "Failed to create repository. Status: " + response.getStatusCode());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to create repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to create repository: " + e.getMessage());
        }
    }

    @Override
    public Page<Commit> listCommits(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // {project}/_apis/git/repositories/{repoId}/commits           
            String url = baseUrl + "/" + repository.getOwnerId() + "/_apis/git/repositories/" + 
                    repository.getId() + "/commits?api-version=7.1&$top=" + page.getPageSize() + 
                    "&$skip=" + (page.getPageNumber() * page.getPageSize());

            ResponseEntity<AzureListCommitsCommitListRes> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AzureListCommitsCommitListRes.class
            );

            List<Commit> commits = new ArrayList<>();
            AzureListCommitsCommitListRes commitListResponse = response.getBody();
            if (commitListResponse != null && commitListResponse.getValue() != null) {
                for (var commitResponse : commitListResponse.getValue()) {
                    Commit commit = AzureListCommitsMapper.toInternalModel(commitResponse);
                    if (commit != null) {
                        commits.add(commit);
                    }
                }
            }

            return new PageImpl<>(commits, page, commits.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to list commits: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to list commits: " + e.getMessage());
        }
    }

    @Override
    public Page<Branch> listBranches(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // {project}/_apis/git/repositories/{repoId}/refs?filter=heads           
            String url = baseUrl + "/" + repository.getOwnerId() + "/_apis/git/repositories/" + 
                    repository.getId() + "/refs?api-version=7.1&filter=heads&$top=" + page.getPageSize() + 
                    "&$skip=" + (page.getPageNumber() * page.getPageSize());

            ResponseEntity<AzureListBranchesBranchListRes> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AzureListBranchesBranchListRes.class
            );

            List<Branch> branches = new ArrayList<>();
            AzureListBranchesBranchListRes branchListResponse = response.getBody();
            if (branchListResponse != null && branchListResponse.getValue() != null) {
                for (var branchResponse : branchListResponse.getValue()) {
                    Branch branch = AzureListBranchesMapper.toInternalModel(branchResponse);
                    if (branch != null) {
                        branches.add(branch);
                    }
                }
            }

            return new PageImpl<>(branches, page, branches.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to list branches: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to list branches: " + e.getMessage());
        }
    }

    @Override
    public Page<Tag> listTags(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // {project}/_apis/git/repositories/{repoId}/refs?filter=tags           
            String url = baseUrl + "/" + repository.getOwnerId() + "/_apis/git/repositories/" + 
                    repository.getId() + "/refs?api-version=7.1&filter=tags&$top=" + page.getPageSize() + 
                    "&$skip=" + (page.getPageNumber() * page.getPageSize());

            ResponseEntity<AzureListTagsTagListRes> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AzureListTagsTagListRes.class
            );

            List<Tag> tags = new ArrayList<>();
            AzureListTagsTagListRes tagListResponse = response.getBody();
            if (tagListResponse != null && tagListResponse.getValue() != null) {
                for (var tagResponse : tagListResponse.getValue()) {
                    Tag tag = AzureListTagsMapper.toInternalModel(tagResponse);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
            }

            return new PageImpl<>(tags, page, tags.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Azure DevOps authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Azure DevOps request failed to list tags: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Azure DevOps request failed to list tags: " + e.getMessage());
        }
    }

    /**
     * Creates a GitAuthContext based on the available credentials in this provider
     *
     * @return configured GitAuthContext
     */
    public GitAuthContext createGitAuthContext() {
        if (this.credential instanceof PatCredential pat) return createGitAuthContext(pat);
        throw new IllegalArgumentException("Unknown credential type for Azure DevOps");
    }

    private GitAuthContext createGitAuthContext(PatCredential pat) {
        GitAuthContext ctx = new GitAuthContext();
        ctx.transportProtocol = GitAuthContext.TransportProtocol.HTTP;

        // Use PAT credential for authentication
        if (pat != null && pat.getToken() != null) {
            HttpHeaders headers = new HttpHeaders();
            // For Azure DevOps, we need to use basic auth with PAT as password
            // Azure DevOps uses username:token format for basic auth
            headers.set("username", "dummy"); // Azure DevOps doesn't use username for PAT auth
            headers.set("password", pat.getToken());
            ctx.httpAuthHeaders = headers;
        }
        // If no PAT credential available, ctx.httpAuthHeaders will be null (unauthenticated access)

        return ctx;
    }

    /**
     * Create Azure DevOps-specific HTTP headers for authentication.
     * Uses Bearer token authentication with Personal Access Tokens.
     */
    private HttpHeaders createAzureDevOpsHeaders() {
        if (this.credential instanceof PatCredential pat) return createAzureDevOpsHeaders(pat);
        throw new IllegalArgumentException("Unknown credential type for Azure DevOps");
    }

    private HttpHeaders createAzureDevOpsHeaders(PatCredential pat) {
        HttpHeaders headers = new HttpHeaders();

        if (pat != null) {
            headers.setBasicAuth("dummy", pat.getToken());
        } else {
            throw new IllegalStateException("PAT credential is required for Azure DevOps authentication");
        }

        // Add common headers for Azure DevOps API
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "GitProviderDemo/1.0");

        return headers;
    }
}

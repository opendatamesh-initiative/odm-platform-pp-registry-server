package org.opendatamesh.platform.pp.registry.githandler.provider.azure;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.model.filters.ListCommitFilters;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
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
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits.AzureListCommitsCommitRes;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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
    private final GitProviderCredential credential;

    public AzureDevOpsProvider(String baseUrl, RestTemplate restTemplate, GitProviderCredential credential) throws BadRequestException {
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
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the connectionData endpoint to verify authentication
            ResponseEntity<AzureCheckConnectionUserResponseRes> response = restTemplate.exchange(
                    baseUrl + "/_apis/connectionData?api-version=7.1-preview.1",
                    HttpMethod.GET,
                    entity,
                    AzureCheckConnectionUserResponseRes.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
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
            HttpHeaders headers = credential.createGitProviderHeaders();
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
            HttpHeaders headers = credential.createGitProviderHeaders();
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
                    String reposUriTemplate = baseUrl + "/{projectName}/_apis/git/repositories?api-version={apiVersion}";
                    Map<String, Object> reposUriVariables = new HashMap<>();
                    reposUriVariables.put("projectName", project.getName());
                    reposUriVariables.put("apiVersion", "7.1");

                    ResponseEntity<AzureListRepositoriesRepositoryListRes> reposResponse = restTemplate.exchange(
                            reposUriTemplate,
                            HttpMethod.GET,
                            entity,
                            AzureListRepositoriesRepositoryListRes.class,
                            reposUriVariables
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
            HttpHeaders headers = credential.createGitProviderHeaders();
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
                    String repoUriTemplate = baseUrl + "/{projectName}/_apis/git/repositories/{repoId}?api-version={apiVersion}";
                    Map<String, Object> repoUriVariables = new HashMap<>();
                    repoUriVariables.put("projectName", project.getName());
                    repoUriVariables.put("repoId", id);
                    repoUriVariables.put("apiVersion", "7.1");

                    try {
                        ResponseEntity<AzureGetRepositoryRepositoryRes> repoResponse = restTemplate.exchange(
                                repoUriTemplate,
                                HttpMethod.GET,
                                entity,
                                AzureGetRepositoryRepositoryRes.class,
                                repoUriVariables
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
            HttpHeaders headers = credential.createGitProviderHeaders();
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

            String uriTemplate = baseUrl + "/{projectId}/_apis/git/repositories?api-version={apiVersion}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("projectId", projectId);
            uriVariables.put("apiVersion", "7.1");

            ResponseEntity<AzureCreateRepositoryRepositoryRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.POST,
                    requestEntity,
                    AzureCreateRepositoryRepositoryRes.class,
                    uriVariables
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
    public Page<Commit> listCommits(Repository repository, ListCommitFilters commitFilters, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            Optional<FromOrToCommitFilters> fromOrToCommitFilters = resolveFromOrToCommitFilters(commitFilters);

            UriTemplateAndVariablesListCommits uriData = buildUriTemplateAndVariablesListCommits(repository, fromOrToCommitFilters, page);
            
            ResponseEntity<AzureListCommitsCommitListRes> response = callApiListCommits(uriData.template, entity, uriData.uriVariables);

            List<Commit> commits = mappingListCommitsToInternalModel(response);

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
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // {project}/_apis/git/repositories/{repoId}/refs?filter=heads           
            String uriTemplate = baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/refs?api-version={apiVersion}&filter={filter}&$top={top}&$skip={skip}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("projectId", repository.getOwnerId());
            uriVariables.put("repoId", repository.getId());
            uriVariables.put("apiVersion", "7.1");
            uriVariables.put("filter", "heads");
            uriVariables.put("top", page.getPageSize());
            uriVariables.put("skip", page.getPageNumber() * page.getPageSize());

            ResponseEntity<AzureListBranchesBranchListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    AzureListBranchesBranchListRes.class,
                    uriVariables
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
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // {project}/_apis/git/repositories/{repoId}/refs?filter=tags           
            String uriTemplate = baseUrl + "/{projectId}/_apis/git/repositories/{repoId}/refs?api-version={apiVersion}&filter={filter}&$top={top}&$skip={skip}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("projectId", repository.getOwnerId());
            uriVariables.put("repoId", repository.getId());
            uriVariables.put("apiVersion", "7.1");
            uriVariables.put("filter", "tags");
            uriVariables.put("top", page.getPageSize());
            uriVariables.put("skip", page.getPageNumber() * page.getPageSize());

            ResponseEntity<AzureListTagsTagListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    AzureListTagsTagListRes.class,
                    uriVariables
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
        return credential.createGitAuthContext();
    }

    public record FromOrToCommitFilters(String from, String to, String fromType, String toType) {}

    private record UriTemplateAndVariablesListCommits(String template, Map<String, Object> uriVariables){}

    private UriTemplateAndVariablesListCommits buildUriTemplateAndVariablesListCommits(Repository repository, Optional<FromOrToCommitFilters> fromOrToCommitFilters, Pageable page){
        StringBuilder uriTemplate = new StringBuilder();
        uriTemplate.append(baseUrl)
            .append("/{projectId}/_apis/git/repositories/{repoId}/commits")
            .append("?api-version={apiVersion}")
            .append("&$top={top}")
            .append("&$skip={skip}");

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("projectId", repository.getOwnerId());
        uriVariables.put("repoId", repository.getId());
        uriVariables.put("apiVersion", "7.1");
        uriVariables.put("top", page.getPageSize());
        uriVariables.put("skip", page.getPageNumber() * page.getPageSize());

        // Dynamically add query parameters only if filters are present
        if (fromOrToCommitFilters.isPresent()) {
            FromOrToCommitFilters filters = fromOrToCommitFilters.get();

            // Add itemVersion (from) parameters if present
            if (StringUtils.hasText(filters.from)) {
                uriTemplate.append("&itemVersion.version={from}")
                        .append("&itemVersion.versionType={fromType}");
                uriVariables.put("from", filters.from);
                uriVariables.put("fromType", filters.fromType);
            }

            // Add compareVersion (to) parameters if present
            if (StringUtils.hasText(filters.to)) {
                uriTemplate.append("&compareVersion.version={to}")
                        .append("&compareVersion.versionType={toType}");
                uriVariables.put("to", filters.to);
                uriVariables.put("toType", filters.toType);
            }
        }

        return new UriTemplateAndVariablesListCommits(uriTemplate.toString(), uriVariables);
    }

    private Optional<FromOrToCommitFilters> resolveFromOrToCommitFilters(ListCommitFilters commitFilters) {
        if (commitFilters != null){
            String from = extractFromCommitFilter(commitFilters);
            String to = extractToCommitFilter(commitFilters);
            String fromType = extractTypeFromCommitFilter(commitFilters);
            String toType = extractTypeToCommitFilter(commitFilters);

            if ((from != null && from.isEmpty()) || (to != null && to.isEmpty())){
                throw new BadRequestException("From or to parameter are empty");
            }

            if ((from != null) || (to != null)) {
                return Optional.of(new FromOrToCommitFilters(from, to, fromType, toType));
            }
        }
        return Optional.empty();
    }

    private String extractFromCommitFilter(ListCommitFilters commitFilters){
        if (commitFilters.fromTagName() != null) {
            return commitFilters.fromTagName();
        }
        if (commitFilters.fromCommitHash() != null) {
            return commitFilters.fromCommitHash();
        }
        return commitFilters.fromBranchName();
    }

    private String extractToCommitFilter(ListCommitFilters commitFilters){
        if (commitFilters.toTagName() != null) {
            return commitFilters.toTagName();
        }
        if (commitFilters.toCommitHash() != null) {
            return commitFilters.toCommitHash();
        }
        return commitFilters.toBranchName();
    }

    private String extractTypeFromCommitFilter(ListCommitFilters commitFilters){
        if (StringUtils.hasText(commitFilters.fromTagName())) {
            return "tag";
        }
        if (StringUtils.hasText(commitFilters.fromCommitHash())) {
            return "commit";
        }
        return "branch";
    }

    private String extractTypeToCommitFilter(ListCommitFilters commitFilters){
        if (StringUtils.hasText(commitFilters.toTagName())) {
            return "tag";
        }
        if (StringUtils.hasText(commitFilters.toCommitHash())) {
            return "commit";
        }
        return "branch";
    }

    private ResponseEntity<AzureListCommitsCommitListRes> callApiListCommits(String uriTemplate, HttpEntity<String> entity, Map<String, Object> uriVariables){
        return restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                AzureListCommitsCommitListRes.class,
                uriVariables
        );
    }

    private List<Commit> mappingListCommitsToInternalModel(ResponseEntity<AzureListCommitsCommitListRes> response){
        List<Commit> commits = new ArrayList<>();
        AzureListCommitsCommitListRes commitResponses = response.getBody();
        if (commitResponses != null) {
            for (AzureListCommitsCommitRes commitResponse : commitResponses.getValue()) {
                Commit commit = AzureListCommitsMapper.toInternalModel(commitResponse);
                if (commit != null) {
                    commits.add(commit);
                }
            }
        }
        return commits;
    }
}

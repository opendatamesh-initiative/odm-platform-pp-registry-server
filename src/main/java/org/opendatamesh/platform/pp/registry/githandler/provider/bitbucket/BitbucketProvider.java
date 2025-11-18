package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket;


import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderCustomResourceReader;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderModelExtension;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderModelResourceType;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.modelextensions.BitbucketRepositoryExtension;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.checkconnection.BitbucketCheckConnectionUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository.BitbucketCreateRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository.BitbucketCreateRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository.BitbucketCreateRepositoryReq;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getcurrentuser.BitbucketGetCurrentUserMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getcurrentuser.BitbucketGetCurrentUserUserRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getorganization.BitbucketGetOrganizationMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getorganization.BitbucketGetOrganizationWorkspaceRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getrepository.BitbucketGetRepositoryMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getrepository.BitbucketGetRepositoryRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches.BitbucketListBranchesBranchListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches.BitbucketListBranchesBranchRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches.BitbucketListBranchesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits.BitbucketListCommitsCommitListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits.BitbucketListCommitsCommitRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits.BitbucketListCommitsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers.BitbucketListMembersMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers.BitbucketListMembersUserListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers.BitbucketListMembersWorkspaceMembershipRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations.BitbucketListOrganizationsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations.BitbucketListOrganizationsWorkspaceListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations.BitbucketListOrganizationsWorkspaceRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects.BitbucketListProjectsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects.BitbucketListProjectsProjectListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects.BitbucketListProjectsProjectRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories.BitbucketListRepositoriesMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories.BitbucketListRepositoriesRepositoryListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories.BitbucketListRepositoriesRepositoryRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags.BitbucketListTagsMapper;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags.BitbucketListTagsTagListRes;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags.BitbucketListTagsTagRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.function.BiFunction;

import static org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.modelextensions.BitbucketRepositoryExtension.ORGANIZATION;
import static org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.modelextensions.BitbucketRepositoryExtension.PROJECT;

/**
 * Bitbucket provider implementation
 * <p>
 * Supported authentication methods:
 * - Basic Authentication (API Token + Email) - ✅ Required
 * <p>
 * Note: Bitbucket Cloud requires basic authentication with:
 * - Username: Your Bitbucket email address
 * - Password: Your API token generated from your Bitbucket account settings
 * (Personal settings > API tokens).
 */
public class BitbucketProvider implements GitProvider {

    private static final Logger logger = LoggerFactory.getLogger(BitbucketProvider.class);

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final GitProviderCredential credential;

    private final List<GitProviderModelExtension> registeredExtensions = List.of(
            new BitbucketRepositoryExtension()
    );
    private final List<GitProviderCustomResourceReader> customResourceReaders = List.of(
            createCustomResourceReader(PROJECT, this::listProjects)
    );

    public BitbucketProvider(String baseUrl, RestTemplate restTemplate, GitProviderCredential credential) throws BadRequestException {
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.bitbucket.org/2.0";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.credential = credential;
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the /user endpoint to verify authentication
            ResponseEntity<BitbucketCheckConnectionUserRes> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    BitbucketCheckConnectionUserRes.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access Bitbucket with our credentials
                return;
            } else {
                throw new GitProviderAuthenticationException("Failed to authenticate with Bitbucket API");
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to check connection: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to check connection: " + e.getMessage());
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<BitbucketGetCurrentUserUserRes> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    BitbucketGetCurrentUserUserRes.class
            );

            BitbucketGetCurrentUserUserRes userResponse = response.getBody();
            if (userResponse == null) {
                throw new ClientException(404, "Failed to get current user");
            }
            return BitbucketGetCurrentUserMapper.toInternalModel(userResponse);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to get current user: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to get current user: " + e.getMessage());
        }
    }

    @Override
    public Page<Organization> listOrganizations(Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/workspaces?page={page}&pagelen={pagelen}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("pagelen", page.getPageSize());

            ResponseEntity<BitbucketListOrganizationsWorkspaceListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketListOrganizationsWorkspaceListRes.class,
                    uriVariables
            );

            List<Organization> organizations = new ArrayList<>();
            BitbucketListOrganizationsWorkspaceListRes workspaceListResponse = response.getBody();
            if (workspaceListResponse != null && workspaceListResponse.getValues() != null) {
                for (BitbucketListOrganizationsWorkspaceRes workspaceResponse : workspaceListResponse.getValues()) {
                    Organization org = BitbucketListOrganizationsMapper.toInternalModel(workspaceResponse);
                    if (org != null) {
                        organizations.add(org);
                    }
                }
            }

            return new PageImpl<>(organizations, page, organizations.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to list organizations: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to list organizations: " + e.getMessage());
        }
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Try different approaches to get the workspace
            // First, try to get the workspace name from the organization list
            String workspaceName = null;
            try {
                // Get the organization name from the listOrganizations method
                Page<Organization> orgs = listOrganizations(Pageable.ofSize(100));
                for (Organization org : orgs.getContent()) {
                    if (org.getId().equals(id)) {
                        workspaceName = org.getName();
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignore errors when trying to get workspace name
            }

            String[] identifiersToTry = {
                    workspaceName, // Try with workspace name first
                    id, // Try with original ID (with braces)
                    id.replaceAll("[{}]", ""), // Try without braces
                    id.replaceAll("[{}]", "").toLowerCase(), // Try lowercase without braces
                    id.replaceAll("[{}]", "").toUpperCase() // Try uppercase without braces
            };

            for (String identifier : identifiersToTry) {
                if (identifier == null) continue;
                try {
                    String uriTemplate = baseUrl + "/workspaces/{identifier}";
                    Map<String, Object> uriVariables = new HashMap<>();
                    uriVariables.put("identifier", identifier);

                    ResponseEntity<BitbucketGetOrganizationWorkspaceRes> response = restTemplate.exchange(
                            uriTemplate,
                            HttpMethod.GET,
                            entity,
                            BitbucketGetOrganizationWorkspaceRes.class,
                            uriVariables
                    );

                    BitbucketGetOrganizationWorkspaceRes workspaceResponse = response.getBody();
                    if (workspaceResponse != null) {
                        Organization org = BitbucketGetOrganizationMapper.toInternalModel(workspaceResponse);
                        if (org != null) {
                            return Optional.of(org);
                        }
                    }
                } catch (Exception e) {
                    // Try next identifier
                    continue;
                }
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to get organization: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to get organization: " + e.getMessage());
        } catch (Exception e) {
            // All attempts failed
        }

        return Optional.empty();
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/workspaces/{workspaceName}/members?page={page}&pagelen={pagelen}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("workspaceName", org.getName());
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("pagelen", page.getPageSize());

            ResponseEntity<BitbucketListMembersUserListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketListMembersUserListRes.class,
                    uriVariables
            );

            List<User> members = new ArrayList<>();
            BitbucketListMembersUserListRes userListResponse = response.getBody();
            if (userListResponse != null && userListResponse.getValues() != null) {
                for (BitbucketListMembersWorkspaceMembershipRes membership : userListResponse.getValues()) {
                    if (membership.getUser() != null) {
                        User user = BitbucketListMembersMapper.toInternalModelFromMembership(membership.getUser());
                        if (user != null) {
                            members.add(user);
                        }
                    }
                }
            }

            return new PageImpl<>(members, page, members.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to list organization members: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to list organization members: " + e.getMessage());
        }
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, MultiValueMap<String, String> parameters, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Build base URL path
            String workspacePath = org != null ? org.getName() : usr.getUsername();

            // Build templated URI - RestTemplate will handle encoding when expanding variables
            String uriTemplate = baseUrl + "/repositories/{workspace}?page={page}&pagelen={pagelen}";

            // Prepare URI variables map
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("workspace", workspacePath);
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("pagelen", page.getPageSize());

            // Build query filter if project parameter is specified
            if (parameters != null && parameters.containsKey(PROJECT)) {
                List<String> projects = parameters.get(PROJECT);
                if (projects != null && !projects.isEmpty()) {
                    // Bitbucket API supports filtering by project using the q parameter
                    // Format: q=project.uuid="PROJECT_UUID"
                    // For multiple projects, we create an OR condition
                    StringBuilder queryBuilder = new StringBuilder();
                    for (int i = 0; i < projects.size(); i++) {
                        if (i > 0) {
                            queryBuilder.append(" OR ");
                        }
                        String project = projects.get(i);
                        // Always use project.uuid for filtering
                        queryBuilder.append("project.uuid=\"").append(project).append("\"");
                    }
                    String queryFilter = queryBuilder.toString();
                    // Add query parameter to template and variables
                    uriTemplate += "&q={query}";
                    uriVariables.put("query", queryFilter);
                }
            }

            // RestTemplate will properly encode all URI variables when expanding the template
            ResponseEntity<BitbucketListRepositoriesRepositoryListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketListRepositoriesRepositoryListRes.class,
                    uriVariables
            );

            List<Repository> repositories = new ArrayList<>();
            BitbucketListRepositoriesRepositoryListRes repoListResponse = response.getBody();
            if (repoListResponse != null && repoListResponse.getValues() != null) {
                OwnerType ownerType = org != null ? OwnerType.ORGANIZATION : OwnerType.ACCOUNT;
                for (BitbucketListRepositoriesRepositoryRes repoResponse : repoListResponse.getValues()) {
                    Repository repo = BitbucketListRepositoriesMapper.toInternalModel(repoResponse, ownerType);
                    if (repo != null) {
                        repositories.add(repo);
                    }
                }
            }

            return new PageImpl<>(repositories, page, repositories.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to list repositories: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to list repositories: " + e.getMessage());
        }
    }

    @Override
    public Optional<Repository> getRepository(String id, String ownerId) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/repositories/{ownerId}/{id}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("ownerId", ownerId);
            uriVariables.put("id", id);

            ResponseEntity<BitbucketGetRepositoryRepositoryRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketGetRepositoryRepositoryRes.class,
                    uriVariables
            );

            BitbucketGetRepositoryRepositoryRes repoResponse = response.getBody();
            if (repoResponse != null) {
                Repository repo = BitbucketGetRepositoryMapper.toInternalModel(repoResponse, OwnerType.ACCOUNT); // Default to ACCOUNT
                if (repo != null) {
                    return Optional.of(repo);
                }
            }
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to get repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to get repository: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Repository createRepository(Repository repositoryToCreate) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            headers.set("Content-Type", "application/json");

            // Bitbucket only supports repositories under a workspace (organization or user)
            // Validate that the owner type is ORGANIZATION (for workspace info)
            if (repositoryToCreate.getOwnerType() != OwnerType.ORGANIZATION) {
                throw new IllegalArgumentException("Bitbucket only supports repositories under a workspace. Provide the workspace information as an ORGANIZATION.");
            }

            // We need to get the workspace from the owner ID
            String workspace = repositoryToCreate.getOwnerId();
            if (workspace == null || workspace.isEmpty()) {
                throw new IllegalArgumentException("Owner ID (workspace) is required for Bitbucket repository creation");
            }

            // Create request payload from internal model
            BitbucketCreateRepositoryReq request = BitbucketCreateRepositoryMapper.createRequestFromInternalModel(repositoryToCreate);

            HttpEntity<BitbucketCreateRepositoryReq> entity = new HttpEntity<>(request, headers);

            String uriTemplate = baseUrl + "/repositories/{workspace}/{repoSlug}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("workspace", workspace);
            uriVariables.put("repoSlug", repositoryToCreate.getName());

            ResponseEntity<BitbucketCreateRepositoryRepositoryRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.POST,
                    entity,
                    BitbucketCreateRepositoryRepositoryRes.class,
                    uriVariables
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                BitbucketCreateRepositoryRepositoryRes repoResponse = response.getBody();
                return BitbucketCreateRepositoryMapper.toInternalModel(repoResponse, repositoryToCreate.getOwnerType());
            }

            throw new ClientException(500, "Failed to create repository. Status: " + response.getStatusCode());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to create repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to create repository: " + e.getMessage());
        }
    }

    @Override
    public Page<Commit> listCommits(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/repositories/{ownerId}/{repoId}/refs/commits";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("ownerId", repository.getOwnerId());
            uriVariables.put("repoId", repository.getId());

            ResponseEntity<BitbucketListCommitsCommitListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketListCommitsCommitListRes.class,
                    uriVariables
            );

            List<Commit> commits = new ArrayList<>();
            BitbucketListCommitsCommitListRes commitListResponse = response.getBody();
            if (commitListResponse != null && commitListResponse.getValues() != null) {
                for (BitbucketListCommitsCommitRes commitResponse : commitListResponse.getValues()) {
                    Commit commit = BitbucketListCommitsMapper.toInternalModel(commitResponse);
                    if (commit != null) {
                        commits.add(commit);
                    }
                }
            }

            return new PageImpl<>(commits, page, commits.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to list commits: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to list commits: " + e.getMessage());
        }
    }

    @Override
    public Page<Branch> listBranches(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/repositories/{ownerId}/{repoId}/refs/branches";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("ownerId", repository.getOwnerId());
            uriVariables.put("repoId", repository.getId());

            ResponseEntity<BitbucketListBranchesBranchListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketListBranchesBranchListRes.class,
                    uriVariables
            );

            List<Branch> branches = new ArrayList<>();
            BitbucketListBranchesBranchListRes branchListResponse = response.getBody();
            if (branchListResponse != null && branchListResponse.getValues() != null) {
                for (BitbucketListBranchesBranchRes branchResponse : branchListResponse.getValues()) {
                    Branch branch = BitbucketListBranchesMapper.toInternalModel(branchResponse);
                    if (branch != null) {
                        branches.add(branch);
                    }
                }
            }

            return new PageImpl<>(branches, page, branches.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to list branches: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to list branches: " + e.getMessage());
        }
    }

    @Override
    public Page<Tag> listTags(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/repositories/{ownerId}/{repoId}/refs/tags";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("ownerId", repository.getOwnerId());
            uriVariables.put("repoId", repository.getId());

            ResponseEntity<BitbucketListTagsTagListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketListTagsTagListRes.class,
                    uriVariables
            );

            List<Tag> tags = new ArrayList<>();
            BitbucketListTagsTagListRes tagListResponse = response.getBody();
            if (tagListResponse != null && tagListResponse.getValues() != null) {
                for (BitbucketListTagsTagRes tagResponse : tagListResponse.getValues()) {
                    Tag tag = BitbucketListTagsMapper.toInternalModel(tagResponse);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
            }

            return new PageImpl<>(tags, page, tags.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to list tags: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to list tags: " + e.getMessage());
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

    @Override
    public List<ProviderCustomResourceDefinition> getProviderCustomResourceDefinitions(GitProviderModelResourceType modelResourceType) {
        return this.registeredExtensions.stream()
                .filter(a -> a.support(modelResourceType))
                .findFirst()
                .map(GitProviderModelExtension::getCustomResourcesDefinitions)
                .orElse(List.of());
    }

    @Override
    public Page<ProviderCustomResource> getProviderCustomResources(String customResourceType, MultiValueMap<String, String> parameters, Pageable pageable) {
        return this.customResourceReaders.stream()
                .filter(a -> a.support(customResourceType))
                .findFirst()
                .map(resourceReader -> resourceReader.getCustomResources(parameters, pageable))
                .orElseThrow(() -> new BadRequestException("Bitbucket Provider, unsupported retrieval for resource type: " + customResourceType));
    }


    private GitProviderCustomResourceReader createCustomResourceReader(
            String resourceType, BiFunction<MultiValueMap<String, String>, Pageable, Page<ProviderCustomResource>> resourceSupplier) {
        return new GitProviderCustomResourceReader() {
            @Override
            public boolean support(String type) {
                return resourceType.equalsIgnoreCase(type);
            }

            @Override
            public Page<ProviderCustomResource> getCustomResources(MultiValueMap<String, String> parameters, Pageable pageable) {
                return resourceSupplier.apply(parameters, pageable);
            }
        };
    }

    private Page<ProviderCustomResource> listProjects(MultiValueMap<String, String> parameters, Pageable pageable) {
        try {
            List<String> workspaceNames = parameters.getOrDefault(ORGANIZATION, Collections.emptyList());
            // Projects are workspace-scoped in Bitbucket, so we need to list projects for each workspace
            // Bitbucket API: GET /2.0/workspaces/{workspace}/projects
            // Note: Projects endpoint may not be available in Bitbucket Cloud (api.bitbucket.org)
            // It's primarily available in Bitbucket Server/Data Center

            // Optimization: If only one workspace is specified, use direct pagination
            if (workspaceNames.size() == 1) {
                return listProjectsForWorkspace(workspaceNames.get(0), pageable);
            }

            // Multiple workspaces or no workspace specified: collect all projects, then paginate
            if (workspaceNames.isEmpty()) {
                logger.info("Bitbucket Git Provider, listing projects: fetching all organizations (workspaces) first, because no organization (workspace) parameter has been passed");
                // If no organizations specified, fetch all workspaces first
                workspaceNames = listOrganizations(Pageable.ofSize(100))
                        .getContent().stream()
                        .map(Organization::getName)
                        .toList();
            }

            return listProjectsFromMultipleWorkspaces(workspaceNames, pageable);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("Bitbucket authentication failed with provider. Please check your credentials.");
            }
            throw new ClientException(e.getStatusCode().value(), "Bitbucket request failed to list projects: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Bitbucket request failed to list projects: " + e.getMessage());
        }
    }

    /**
     * List projects from multiple workspaces by collecting all projects and applying in-memory pagination.
     * This method is used when multiple workspaces are specified or when all workspaces need to be queried.
     */
    private Page<ProviderCustomResource> listProjectsFromMultipleWorkspaces(List<String> workspaceNames, Pageable pageable) {
        // Collect all projects from all workspaces
        List<ProviderCustomResource> allProjects = new ArrayList<>();
        for (String workspaceName : workspaceNames) {
            allProjects.addAll(listAllProjectsForWorkspace(workspaceName));
        }

        // Apply in-memory pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProjects.size());
        List<ProviderCustomResource> pagedProjects = start < allProjects.size()
                ? allProjects.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(pagedProjects, pageable, allProjects.size());
    }

    /**
     * List projects for a single workspace with proper pagination.
     * This method uses direct API pagination when only one workspace is queried.
     */
    private Page<ProviderCustomResource> listProjectsForWorkspace(String workspaceName, Pageable pageable) {
        HttpHeaders headers = credential.createGitProviderHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Use the pageable to construct the API request
        int bitbucketPage = pageable.getPageNumber() + 1; // Bitbucket uses 1-based pagination
        int pageSize = pageable.getPageSize();
        String uriTemplate = baseUrl + "/workspaces/{workspaceName}/projects?page={page}&pagelen={pagelen}";
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("workspaceName", workspaceName);
        uriVariables.put("page", bitbucketPage);
        uriVariables.put("pagelen", pageSize);

        ResponseEntity<BitbucketListProjectsProjectListRes> response = restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                BitbucketListProjectsProjectListRes.class,
                uriVariables
        );

        List<ProviderCustomResource> projects = new ArrayList<>();
        BitbucketListProjectsProjectListRes projectListResponse = response.getBody();
        int totalSize = 0;

        if (projectListResponse != null && projectListResponse.getValues() != null) {
            for (BitbucketListProjectsProjectRes projectRes : projectListResponse.getValues()) {
                ProviderCustomResource customResource = BitbucketListProjectsMapper.toProviderCustomResource(projectRes);
                if (customResource != null) {
                    projects.add(customResource);
                }
            }
            // Use the size from the response if available, otherwise use the current page size
            totalSize = projectListResponse.getSize() != null ? projectListResponse.getSize() : projects.size();
        }

        return new PageImpl<>(projects, pageable, totalSize);
    }

    /**
     * Fetch all projects for a workspace (used when multiple workspaces need to be aggregated).
     * This method paginates through all pages from the Bitbucket API.
     */
    private List<ProviderCustomResource> listAllProjectsForWorkspace(String workspaceName) {
        HttpHeaders headers = credential.createGitProviderHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<ProviderCustomResource> projects = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            String uriTemplate = baseUrl + "/workspaces/{workspaceName}/projects?page={page}&pagelen={pagelen}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("workspaceName", workspaceName);
            uriVariables.put("page", page);
            uriVariables.put("pagelen", 100);

            ResponseEntity<BitbucketListProjectsProjectListRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    BitbucketListProjectsProjectListRes.class,
                    uriVariables
            );

            BitbucketListProjectsProjectListRes projectListResponse = response.getBody();
            if (projectListResponse != null && projectListResponse.getValues() != null) {
                int currentPageSize = projectListResponse.getValues().size();

                for (BitbucketListProjectsProjectRes projectRes : projectListResponse.getValues()) {
                    ProviderCustomResource customResource = BitbucketListProjectsMapper.toProviderCustomResource(projectRes);
                    if (customResource != null) {
                        projects.add(customResource);
                    }
                }

                // Check if there are more pages for this workspace
                Integer totalSize = projectListResponse.getSize();
                if (totalSize != null && totalSize > 0) {
                    // If we have a total size, check if we've fetched all items
                    int fetchedSoFar = (page - 1) * 100 + currentPageSize;
                    hasMore = fetchedSoFar < totalSize;
                } else {
                    // If no total size info, assume more pages if current page is full
                    hasMore = currentPageSize == 100;
                }
                page++;
            } else {
                hasMore = false;
            }
        }

        return projects;
    }
}

package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
    private final PatCredential patCredential;

    public GitLabProvider(String baseUrl, RestTemplate restTemplate, PatCredential patCredential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://gitlab.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.patCredential = patCredential;
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the /user endpoint to verify authentication
            ResponseEntity<GitLabUserResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v4/user",
                    HttpMethod.GET,
                    entity,
                    GitLabUserResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access GitLab with our credentials
                return;
            } else {
                throw new RuntimeException("Failed to authenticate with GitLab API");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to GitLab: " + e.getMessage(), e);
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitLabUserResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v4/user",
                    HttpMethod.GET,
                    entity,
                    GitLabUserResponse.class
            );

            GitLabUserResponse userResponse = response.getBody();
            if (userResponse != null) {
                return new User(
                        String.valueOf(userResponse.getId()),
                        userResponse.getUsername(),
                        userResponse.getName(),
                        userResponse.getAvatarUrl(),
                        userResponse.getWebUrl()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current user", e);
        }

        throw new RuntimeException("Failed to get current user");
    }

    @Override
    public Page<Organization> listOrganizations(Pageable page) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/api/v4/groups?page=" + (page.getPageNumber() + 1) +
                    "&per_page=" + page.getPageSize() + "&owned=true";

            ResponseEntity<GitLabGroupResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabGroupResponse[].class
            );

            List<Organization> organizations = new ArrayList<>();
            GitLabGroupResponse[] groupResponses = response.getBody();
            if (groupResponses != null) {
                for (GitLabGroupResponse groupResponse : groupResponses) {
                    organizations.add(new Organization(
                            String.valueOf(groupResponse.getId()),
                            groupResponse.getName(),
                            groupResponse.getWebUrl()
                    ));
                }
            }

            return new PageImpl<>(organizations, page, organizations.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list organizations", e);
        }
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitLabGroupResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v4/groups/" + id,
                    HttpMethod.GET,
                    entity,
                    GitLabGroupResponse.class
            );

            GitLabGroupResponse groupResponse = response.getBody();
            if (groupResponse != null) {
                return Optional.of(new Organization(
                        String.valueOf(groupResponse.getId()),
                        groupResponse.getName(),
                        groupResponse.getWebUrl()
                ));
            }
        } catch (Exception e) {
            // Group not found or other error
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

            ResponseEntity<GitLabUserResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabUserResponse[].class
            );

            List<User> members = new ArrayList<>();
            GitLabUserResponse[] userResponses = response.getBody();
            if (userResponses != null) {
                for (GitLabUserResponse userResponse : userResponses) {
                    members.add(new User(
                            String.valueOf(userResponse.getId()),
                            userResponse.getUsername(),
                            userResponse.getName(),
                            userResponse.getAvatarUrl(),
                            userResponse.getWebUrl()
                    ));
                }
            }

            return new PageImpl<>(members, page, members.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list organization members", e);
        }
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, Pageable page) {
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

            ResponseEntity<GitLabProjectResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabProjectResponse[].class
            );

            List<Repository> repositories = new ArrayList<>();
            GitLabProjectResponse[] projectResponses = response.getBody();
            if (projectResponses != null) {
                for (GitLabProjectResponse projectResponse : projectResponses) {
                    repositories.add(new Repository(
                            String.valueOf(projectResponse.getId()),
                            projectResponse.getName(),
                            projectResponse.getDescription(),
                            projectResponse.getHttpUrlToRepo(),
                            projectResponse.getSshUrlToRepo(),
                            projectResponse.getDefaultBranch(),
                            org != null ? OwnerType.ORGANIZATION :
                                    OwnerType.ACCOUNT,
                            projectResponse.getCreatorId() != null ? String.valueOf(projectResponse.getCreatorId()) :
                                    (projectResponse.getNamespace() != null ? String.valueOf(projectResponse.getNamespace().getId()) : null),
                            projectResponse.getVisibility().equals("private") ?
                                    Visibility.PRIVATE :
                                    Visibility.PUBLIC
                    ));
                }
            }

            return new PageImpl<>(repositories, page, repositories.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list repositories", e);
        }
    }

    @Override
    public Optional<Repository> getRepository(String id) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitLabProjectResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v4/projects/" + id,
                    HttpMethod.GET,
                    entity,
                    GitLabProjectResponse.class
            );

            GitLabProjectResponse projectResponse = response.getBody();
            if (projectResponse != null) {
                return Optional.of(new Repository(
                        String.valueOf(projectResponse.getId()),
                        projectResponse.getName(),
                        projectResponse.getDescription(),
                        projectResponse.getHttpUrlToRepo(),
                        projectResponse.getSshUrlToRepo(),
                        projectResponse.getDefaultBranch(),
                        OwnerType.ACCOUNT, // Default to ACCOUNT
                        projectResponse.getCreatorId() != null ? String.valueOf(projectResponse.getCreatorId()) :
                                (projectResponse.getNamespace() != null ? String.valueOf(projectResponse.getNamespace().getId()) : null),
                        projectResponse.getVisibility().equals("private") ?
                                Visibility.PRIVATE :
                                Visibility.PUBLIC
                ));
            }
        } catch (Exception e) {
            // Project not found or other error
        }

        return Optional.empty();
    }

    @Override
    public Repository createRepository(Repository repositoryToCreate) {
        try {
            HttpHeaders headers = createGitLabHeaders();
            headers.set("Content-Type", "application/json");

            // Create request payload
            GitLabCreateProjectRequest request = new GitLabCreateProjectRequest();
            request.name = repositoryToCreate.getName();
            request.description = repositoryToCreate.getDescription();
            request.visibility = repositoryToCreate.getVisibility() == Visibility.PRIVATE ? "private" : "public";

            // Set namespace_id based on owner type
            if (repositoryToCreate.getOwnerType() == OwnerType.ORGANIZATION) {
                // For organization (group) projects, set the namespace_id to the group ID
                request.namespaceId = repositoryToCreate.getOwnerId();
            } else {
                // For user projects, omit namespace_id to create under authenticated user's namespace
                // This is the recommended approach according to GitLab API documentation
                request.namespaceId = null;
            }

            HttpEntity<GitLabCreateProjectRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<GitLabProjectResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v4/projects",
                    HttpMethod.POST,
                    entity,
                    GitLabProjectResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GitLabProjectResponse projectResponse = response.getBody();
                return new Repository(
                        String.valueOf(projectResponse.getId()),
                        projectResponse.getName(),
                        projectResponse.getDescription(),
                        projectResponse.getHttpUrlToRepo(),
                        projectResponse.getSshUrlToRepo(),
                        projectResponse.getDefaultBranch(),
                        repositoryToCreate.getOwnerType(), // Use the input owner type
                        projectResponse.getCreatorId() != null ? String.valueOf(projectResponse.getCreatorId()) :
                                (projectResponse.getNamespace() != null ? String.valueOf(projectResponse.getNamespace().getId()) : null),
                        projectResponse.getVisibility().equals("private") ?
                                Visibility.PRIVATE :
                                Visibility.PUBLIC
                );
            }

            throw new RuntimeException("Failed to create repository. Status: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create repository: " + e.getMessage(), e);
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

            ResponseEntity<GitLabCommitResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabCommitResponse[].class
            );

            List<Commit> commits = new ArrayList<>();
            GitLabCommitResponse[] commitResponses = response.getBody();
            if (commitResponses != null) {
                for (GitLabCommitResponse commitResponse : commitResponses) {
                    commits.add(new Commit(
                            commitResponse.getId(),
                            commitResponse.getMessage(),
                            commitResponse.getAuthorEmail(),
                            commitResponse.getAuthoredDate()
                    ));
                }
            }

            return new PageImpl<>(commits, page, commits.size());
        } catch (RestClientResponseException e) {
            throw new ClientException(e.getStatusCode().value(), "Failed to list commits: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Failed to list commits: " + e.getMessage());
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

            ResponseEntity<GitLabBranchResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabBranchResponse[].class
            );

            List<Branch> branches = new ArrayList<>();
            GitLabBranchResponse[] branchResponses = response.getBody();
            if (branchResponses != null) {
                for (GitLabBranchResponse branchResponse : branchResponses) {
                    Branch branch = new Branch(
                            branchResponse.getName(),
                            branchResponse.getCommit().getId()
                    );
                    branch.setProtected(branchResponse.isProtected());
                    branch.setDefault(branchResponse.isDefault());
                    branches.add(branch);
                }
            }

            return new PageImpl<>(branches, page, branches.size());
        } catch (RestClientResponseException e) {
            throw new ClientException(e.getStatusCode().value(), "Failed to list branches: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Failed to list branches: " + e.getMessage());
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

            ResponseEntity<GitLabTagResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitLabTagResponse[].class
            );

            List<Tag> tags = new ArrayList<>();
            GitLabTagResponse[] tagResponses = response.getBody();
            if (tagResponses != null) {
                for (GitLabTagResponse tagResponse : tagResponses) {
                    // Use commit hash from commit object, fallback to target field if commit is null
                    String commitHash = tagResponse.getCommit() != null ? 
                            tagResponse.getCommit().getId() : tagResponse.getTarget();
                    
                    Tag tag = new Tag(
                            tagResponse.getName(),
                            commitHash
                    );
                    tags.add(tag);
                }
            }

            return new PageImpl<>(tags, page, tags.size());
        } catch (RestClientResponseException e) {
            throw new ClientException(e.getStatusCode().value(), "Failed to list tags: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, "Failed to list tags: " + e.getMessage());
        }
    }

    /**
     * Create GitLab-specific HTTP headers for authentication.
     * Uses Bearer token authentication with Personal Access Tokens.
     */
    private HttpHeaders createGitLabHeaders() {
        HttpHeaders headers = new HttpHeaders();

        if (patCredential != null) {
            headers.setBearerAuth(patCredential.getToken());
        } else {
            //TODO handle Oauth2 and token caching
            throw new IllegalStateException("PAT credential is required for GitLab authentication");
        }

        // Add common headers for GitLab API
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "GitProviderDemo/1.0");

        return headers;
    }

    // GitLab API response classes
    private static class GitLabUserResponse {
        private long id;
        private String username;
        private String name;
        private String avatarUrl;
        private String webUrl;

        // Getters and setters
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public void setWebUrl(String webUrl) {
            this.webUrl = webUrl;
        }
    }

    private static class GitLabGroupResponse {
        private long id;
        private String name;
        @JsonProperty("web_url")
        private String webUrl;

        // Getters and setters
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public void setWebUrl(String webUrl) {
            this.webUrl = webUrl;
        }
    }

    private static class GitLabNamespaceResponse {
        private long id;
        private String name;
        private String path;
        private String kind;
        private String fullPath;
        private Long parentId;
        private String avatarUrl;
        private String webUrl;

        // Getters and setters
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getWebUrl() {
            return webUrl;
        }

        public void setWebUrl(String webUrl) {
            this.webUrl = webUrl;
        }
    }

    private static class GitLabProjectResponse {
        private long id;
        private String name;
        private String description;
        @JsonProperty("http_url_to_repo")
        private String httpUrlToRepo;
        @JsonProperty("ssh_url_to_repo")
        private String sshUrlToRepo;
        @JsonProperty("default_branch")
        private String defaultBranch;
        private String visibility;
        private GitLabUserResponse owner;
        private GitLabNamespaceResponse namespace;
        @JsonProperty("creator_id")
        private Long creatorId;

        // Getters and setters
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getHttpUrlToRepo() {
            return httpUrlToRepo;
        }

        public void setHttpUrlToRepo(String httpUrlToRepo) {
            this.httpUrlToRepo = httpUrlToRepo;
        }

        public String getSshUrlToRepo() {
            return sshUrlToRepo;
        }

        public void setSshUrlToRepo(String sshUrlToRepo) {
            this.sshUrlToRepo = sshUrlToRepo;
        }

        public String getDefaultBranch() {
            return defaultBranch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }

        public String getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = visibility;
        }

        public GitLabUserResponse getOwner() {
            return owner;
        }

        public void setOwner(GitLabUserResponse owner) {
            this.owner = owner;
        }

        public GitLabNamespaceResponse getNamespace() {
            return namespace;
        }

        public void setNamespace(GitLabNamespaceResponse namespace) {
            this.namespace = namespace;
        }

        public Long getCreatorId() {
            return creatorId;
        }

        public void setCreatorId(Long creatorId) {
            this.creatorId = creatorId;
        }
    }

    private static class GitLabCreateProjectRequest {
        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("visibility")
        private String visibility;

        @JsonProperty("namespace_id")
        private String namespaceId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = visibility;
        }

        public String getNamespaceId() {
            return namespaceId;
        }

        public void setNamespaceId(String namespaceId) {
            this.namespaceId = namespaceId;
        }
    }

    // Response classes for GitLab API

    public static class GitLabCommitResponse {
        private String id;
        private String message;
        private String author_name;
        private String author_email;
        private java.util.Date authored_date;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getAuthorName() { return author_name; }
        public void setAuthorName(String author_name) { this.author_name = author_name; }
        public String getAuthorEmail() { return author_email; }
        public void setAuthorEmail(String author_email) { this.author_email = author_email; }
        public java.util.Date getAuthoredDate() { return authored_date; }
        public void setAuthoredDate(java.util.Date authored_date) { this.authored_date = authored_date; }
    }

    public static class GitLabBranchResponse {
        private String name;
        private GitLabBranchCommit commit;
        private boolean isProtected;
        private boolean isDefault;
        private String web_url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public GitLabBranchCommit getCommit() { return commit; }
        public void setCommit(GitLabBranchCommit commit) { this.commit = commit; }
        public boolean isProtected() { return isProtected; }
        public void setProtected(boolean isProtected) { this.isProtected = isProtected; }
        public boolean isDefault() { return isDefault; }
        public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
        public String getWebUrl() { return web_url; }
        public void setWebUrl(String web_url) { this.web_url = web_url; }
    }

    public static class GitLabBranchCommit {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    public static class GitLabTagResponse {
        private String name;
        private String message;
        private GitLabTagCommit commit;
        private String target;
        private String web_url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public GitLabTagCommit getCommit() { return commit; }
        public void setCommit(GitLabTagCommit commit) { this.commit = commit; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public String getWebUrl() { return web_url; }
        public void setWebUrl(String web_url) { this.web_url = web_url; }
    }

    public static class GitLabTagCommit {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}

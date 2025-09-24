package org.opendatamesh.platform.pp.registry.githandler.provider.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperationFactory;
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

import java.io.File;
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
    private final PatCredential patCredential;

    public GitHubProvider(String baseUrl, RestTemplate restTemplate, PatCredential patCredential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.github.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.patCredential = patCredential;
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the /user endpoint to verify authentication
            ResponseEntity<GitHubUserResponse> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    GitHubUserResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access the API with our credentials
                return;
            } else {
                throw new RuntimeException("Failed to authenticate with GitHub API");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to GitHub: " + e.getMessage(), e);
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitHubUserResponse> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    GitHubUserResponse.class
            );

            GitHubUserResponse userResponse = response.getBody();
            if (userResponse != null) {
                return new User(
                        String.valueOf(userResponse.getId()),
                        userResponse.getLogin(),
                        userResponse.getName() != null ? userResponse.getName() : userResponse.getLogin(),
                        userResponse.getAvatarUrl(),
                        userResponse.getHtmlUrl()
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GitHub API Limitation: The /user/orgs endpoint returns limited organization information
            // It only provides basic fields (id, login) and may not include html_url or other detailed fields
            // For complete organization details, use getOrganization(String id) method instead
            String url = baseUrl + "/user/orgs?page=" + (page.getPageNumber() + 1) +
                    "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubOrganizationResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubOrganizationResponse[].class
            );

            List<Organization> organizations = new ArrayList<>();
            GitHubOrganizationResponse[] orgResponses = response.getBody();
            if (orgResponses != null) {
                for (GitHubOrganizationResponse orgResponse : orgResponses) {
                    organizations.add(new Organization(
                            String.valueOf(orgResponse.getId()),
                            orgResponse.getLogin(),
                            orgResponse.getHtmlUrl() // May be null due to API limitation
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GitHub API: The /orgs/{id} endpoint provides complete organization details
            // This includes all available fields like html_url, description, etc.
            // This is the recommended endpoint when you need full organization information
            ResponseEntity<GitHubOrganizationResponse> response = restTemplate.exchange(
                    baseUrl + "/orgs/" + id,
                    HttpMethod.GET,
                    entity,
                    GitHubOrganizationResponse.class
            );

            GitHubOrganizationResponse orgResponse = response.getBody();
            if (orgResponse != null) {
                return Optional.of(new Organization(
                        String.valueOf(orgResponse.getId()),
                        orgResponse.getLogin(),
                        orgResponse.getHtmlUrl() // Complete information available
                ));
            }
        } catch (Exception e) {
            // Organization not found or other error
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

            ResponseEntity<GitHubUserResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubUserResponse[].class
            );

            List<User> members = new ArrayList<>();
            GitHubUserResponse[] userResponses = response.getBody();
            if (userResponses != null) {
                for (GitHubUserResponse userResponse : userResponses) {
                    members.add(new User(
                            String.valueOf(userResponse.getId()),
                            userResponse.getLogin(),
                            userResponse.getName() != null ? userResponse.getName() : userResponse.getLogin(),
                            userResponse.getAvatarUrl(),
                            userResponse.getHtmlUrl()
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

            ResponseEntity<GitHubRepositoryResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubRepositoryResponse[].class
            );

            List<Repository> repositories = new ArrayList<>();
            GitHubRepositoryResponse[] repoResponses = response.getBody();
            if (repoResponses != null) {
                for (GitHubRepositoryResponse repoResponse : repoResponses) {
                    repositories.add(new Repository(
                            String.valueOf(repoResponse.getId()),
                            repoResponse.getName(),
                            repoResponse.getDescription(),
                            repoResponse.getCloneUrl(),
                            repoResponse.getSshUrl(),
                            repoResponse.getDefaultBranch(),
                            determineOwnerType(repoResponse.getOwner()),
                            String.valueOf(repoResponse.getOwner().getId()),
                            repoResponse.isPrivate() ? Visibility.PRIVATE :
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitHubRepositoryResponse> response = restTemplate.exchange(
                    baseUrl + "/repositories/" + id,
                    HttpMethod.GET,
                    entity,
                    GitHubRepositoryResponse.class
            );

            GitHubRepositoryResponse repoResponse = response.getBody();
            if (repoResponse != null) {
                return Optional.of(new Repository(
                        String.valueOf(repoResponse.getId()),
                        repoResponse.getName(),
                        repoResponse.getDescription(),
                        repoResponse.getCloneUrl(),
                        repoResponse.getSshUrl(),
                        repoResponse.getDefaultBranch(),
                        determineOwnerType(repoResponse.getOwner()),
                        String.valueOf(repoResponse.getOwner().getId()),
                        repoResponse.isPrivate() ? Visibility.PRIVATE :
                                Visibility.PUBLIC
                ));
            }
        } catch (Exception e) {
            // Repository not found or other error
        }

        return Optional.empty();
    }

    @Override
    public Repository createRepository(Repository repositoryToCreate) {
        try {
            HttpHeaders headers = createGitHubHeaders();
            headers.set("Content-Type", "application/json");
            
            // Create request payload
            GitHubCreateRepositoryRequest request = new GitHubCreateRepositoryRequest();
            request.name = repositoryToCreate.getName();
            request.description = repositoryToCreate.getDescription();
            request.isPrivate = repositoryToCreate.getVisibility() == Visibility.PRIVATE;
            
            HttpEntity<GitHubCreateRepositoryRequest> entity = new HttpEntity<>(request, headers);
            
            // Determine the correct endpoint based on owner type
            String endpoint;
            if (repositoryToCreate.getOwnerType() == OwnerType.ORGANIZATION && repositoryToCreate.getOwnerId() != null) {
                // Create repository under organization
                endpoint = baseUrl + "/orgs/" + repositoryToCreate.getOwnerId() + "/repos";
            } else {
                // Create repository under authenticated user
                endpoint = baseUrl + "/user/repos";
            }
            
            ResponseEntity<GitHubRepositoryResponse> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                GitHubRepositoryResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GitHubRepositoryResponse repoResponse = response.getBody();
                return new Repository(
                    String.valueOf(repoResponse.getId()),
                    repoResponse.getName(),
                    repoResponse.getDescription(),
                    repoResponse.getCloneUrl(),
                    repoResponse.getSshUrl(),
                    repoResponse.getDefaultBranch(),
                    determineOwnerType(repoResponse.getOwner()),
                    String.valueOf(repoResponse.getOwner().getId()),
                    repoResponse.isPrivate() ? Visibility.PRIVATE : Visibility.PUBLIC
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
            HttpHeaders headers = createGitHubHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Determine owner from org or user
            String owner = (org != null) ? org.getName() : usr.getUsername();
            String repoName = repository.getName();
            
            String url = baseUrl + "/repos/" + owner + "/" + repoName + "/commits?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubCommitResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubCommitResponse[].class
            );

            List<Commit> commits = new ArrayList<>();
            GitHubCommitResponse[] commitResponses = response.getBody();
            if (commitResponses != null) {
                for (GitHubCommitResponse commitResponse : commitResponses) {
                    commits.add(new Commit(
                            commitResponse.getSha(),
                            commitResponse.getCommit().getMessage(),
                            commitResponse.getCommit().getAuthor().getName(),
                            commitResponse.getCommit().getAuthor().getEmail(),
                            commitResponse.getCommit().getAuthor().getDate()
                    ));
                }
            }

            return new PageImpl<>(commits, page, commits.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list commits", e);
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
            
            String url = baseUrl + "/repos/" + owner + "/" + repoName + "/branches?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubBranchResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubBranchResponse[].class
            );

            List<Branch> branches = new ArrayList<>();
            GitHubBranchResponse[] branchResponses = response.getBody();
            if (branchResponses != null) {
                for (GitHubBranchResponse branchResponse : branchResponses) {
                    Branch branch = new Branch(
                            branchResponse.getName(),
                            branchResponse.getCommit().getSha()
                    );
                    branch.setProtected(branchResponse.isProtected());
                    branch.setUrl(branchResponse.getCommit().getUrl());
                    branches.add(branch);
                }
            }

            return new PageImpl<>(branches, page, branches.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list branches", e);
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
            
            String url = baseUrl + "/repos/" + owner + "/" + repoName + "/tags?page=" +
                    (page.getPageNumber() + 1) + "&per_page=" + page.getPageSize();

            ResponseEntity<GitHubTagResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubTagResponse[].class
            );

            List<Tag> tags = new ArrayList<>();
            GitHubTagResponse[] tagResponses = response.getBody();
            if (tagResponses != null) {
                for (GitHubTagResponse tagResponse : tagResponses) {
                    Tag tag = new Tag(
                            tagResponse.getName(),
                            tagResponse.getCommit().getSha()
                    );
                    tag.setUrl(tagResponse.getZipballUrl());
                    tags.add(tag);
                }
            }

            return new PageImpl<>(tags, page, tags.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list tags", e);
        }
    }

    /**
     * Create GitHub-specific HTTP headers for authentication.
     * Supports both Bearer token and Basic authentication.
     */
    private HttpHeaders createGitHubHeaders() {
        HttpHeaders headers = new HttpHeaders();

        if (patCredential != null) {
            headers.setBearerAuth(patCredential.getToken());
        } else {
            //TODO handle oauth2 + bearer token caching
            throw new IllegalStateException("Unsupported authentication type");
        }

        // Add common headers for GitHub API
        headers.set("Accept", "application/vnd.github.v3+json");//TODO
        headers.set("User-Agent", "GitProviderDemo/1.0");//TODO

        return headers;
    }

    private OwnerType determineOwnerType(GitHubUserResponse owner) {
        if (owner != null && "Organization".equals(owner.getType())) {
            return OwnerType.ORGANIZATION;
        }
        return OwnerType.ACCOUNT;
    }

    // GitHub API response classes
    private static class GitHubUserResponse {
        private long id;
        private String login;
        private String name;
        private String type; // "User" or "Organization"

        @JsonProperty("avatar_url")
        private String avatarUrl;

        @JsonProperty("html_url")
        private String htmlUrl;

        // Getters and setters
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }
    }

    private static class GitHubOrganizationResponse {
        private long id;
        private String login;

        @JsonProperty("html_url")
        private String htmlUrl;

        // Getters and setters
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }
    }

    private static class GitHubRepositoryResponse {
        private long id;
        private String name;
        private String description;

        @JsonProperty("clone_url")
        private String clone_url;  // GitHub API uses snake_case

        @JsonProperty("ssh_url")
        private String ssh_url;    // GitHub API uses snake_case

        @JsonProperty("default_branch")
        private String default_branch; // GitHub API uses snake_case

        @JsonProperty("private")
        private boolean isPrivate;
        private GitHubUserResponse owner;

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

        public String getCloneUrl() {
            return clone_url;
        }

        public void setCloneUrl(String cloneUrl) {
            this.clone_url = cloneUrl;
        }

        public String getSshUrl() {
            return ssh_url;
        }

        public void setSshUrl(String sshUrl) {
            this.ssh_url = sshUrl;
        }

        public String getDefaultBranch() {
            return default_branch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.default_branch = defaultBranch;
        }

        public boolean isPrivate() {
            return isPrivate;
        }

        public void setPrivate(boolean aPrivate) {
            isPrivate = aPrivate;
        }

        public GitHubUserResponse getOwner() {
            return owner;
        }

        public void setOwner(GitHubUserResponse owner) {
            this.owner = owner;
        }
    }

    private static class GitHubCreateRepositoryRequest {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("private")
        private boolean isPrivate;

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

        public boolean isPrivate() {
            return isPrivate;
        }

        public void setPrivate(boolean aPrivate) {
            isPrivate = aPrivate;
        }
    }

    @Override
    public File readRepository(RepositoryPointer pointer) {
        if (pointer == null || pointer.getRepository() == null) {
            throw new IllegalArgumentException("RepositoryPointer and Repository cannot be null");
        }

        // Create GitOperation using factory
        GitOperation gitOperation = GitOperationFactory.createGitOperation();
        
        // Create GitAuthContext based on available credentials
        GitAuthContext authContext = createGitAuthContext();
        
        // Use GitOperation to clone and checkout the repository
        return gitOperation.getRepositoryContent(pointer, authContext);
    }
    
    /**
     * Creates a GitAuthContext based on the available credentials in this provider
     * 
     * @return configured GitAuthContext
     */
    private GitAuthContext createGitAuthContext() {
        GitAuthContext ctx = new GitAuthContext();
        ctx.transportProtocol = GitAuthContext.TransportProtocol.HTTP;
        
        // Use PAT credential for authentication
        if (patCredential != null && patCredential.getToken() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + patCredential.getToken());
            ctx.httpAuthHeaders = headers;
        }
        // If no PAT credential available, ctx.httpAuthHeaders will be null (unauthenticated access)
        // Note: OAuth credential is for OAuth flow configuration, not for storing access tokens
        
        return ctx;
    }


    // Response classes for GitHub API

    public static class GitHubCommitResponse {
        private String sha;
        private GitHubCommit commit;
        private String url;

        public String getSha() { return sha; }
        public void setSha(String sha) { this.sha = sha; }
        public GitHubCommit getCommit() { return commit; }
        public void setCommit(GitHubCommit commit) { this.commit = commit; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class GitHubCommit {
        private GitHubCommitAuthor author;
        private GitHubCommitAuthor committer;
        private String message;

        public GitHubCommitAuthor getAuthor() { return author; }
        public void setAuthor(GitHubCommitAuthor author) { this.author = author; }
        public GitHubCommitAuthor getCommitter() { return committer; }
        public void setCommitter(GitHubCommitAuthor committer) { this.committer = committer; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class GitHubCommitAuthor {
        private String name;
        private String email;
        private java.util.Date date;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public java.util.Date getDate() { return date; }
        public void setDate(java.util.Date date) { this.date = date; }
    }

    public static class GitHubBranchResponse {
        private String name;
        private GitHubBranchCommit commit;
        private boolean isProtected;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public GitHubBranchCommit getCommit() { return commit; }
        public void setCommit(GitHubBranchCommit commit) { this.commit = commit; }
        public boolean isProtected() { return isProtected; }
        public void setProtected(boolean isProtected) { this.isProtected = isProtected; }
    }

    public static class GitHubBranchCommit {
        private String sha;
        private String url;

        public String getSha() { return sha; }
        public void setSha(String sha) { this.sha = sha; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class GitHubTagResponse {
        private String name;
        private GitHubTagCommit commit;
        private String zipball_url;
        private String tarball_url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public GitHubTagCommit getCommit() { return commit; }
        public void setCommit(GitHubTagCommit commit) { this.commit = commit; }
        public String getZipballUrl() { return zipball_url; }
        public void setZipballUrl(String zipball_url) { this.zipball_url = zipball_url; }
        public String getTarballUrl() { return tarball_url; }
        public void setTarballUrl(String tarball_url) { this.tarball_url = tarball_url; }
    }

    public static class GitHubTagCommit {
        private String sha;
        private String url;

        public String getSha() { return sha; }
        public void setSha(String sha) { this.sha = sha; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}

package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final PatCredential patCredential;

    public BitbucketProvider(String baseUrl, RestTemplate restTemplate, PatCredential patCredential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.bitbucket.org/2.0";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.patCredential = patCredential;
    }

    @Override
    public void checkConnection() {
        try {
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use the /user endpoint to verify authentication
            ResponseEntity<BitbucketUserResponse> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    BitbucketUserResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access Bitbucket with our credentials
                return;
            } else {
                throw new RuntimeException("Failed to authenticate with Bitbucket API");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Bitbucket: " + e.getMessage(), e);
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<BitbucketUserResponse> response = restTemplate.exchange(
                    baseUrl + "/user",
                    HttpMethod.GET,
                    entity,
                    BitbucketUserResponse.class
            );

            BitbucketUserResponse userResponse = response.getBody();
            if (userResponse != null) {
                String avatarUrl = null;
                String htmlUrl = null;

                if (userResponse.getLinks() != null) {
                    if (userResponse.getLinks().getAvatar() != null) {
                        avatarUrl = userResponse.getLinks().getAvatar().getHref();
                    }
                    if (userResponse.getLinks().getHtml() != null) {
                        htmlUrl = userResponse.getLinks().getHtml().getHref();
                    }
                }

                return new User(
                        userResponse.getUuid(),
                        userResponse.getUsername(),
                        userResponse.getDisplayName(),
                        avatarUrl,
                        htmlUrl
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
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/workspaces?page=" + (page.getPageNumber() + 1) +
                    "&pagelen=" + page.getPageSize();

            ResponseEntity<BitbucketWorkspaceListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BitbucketWorkspaceListResponse.class
            );

            List<Organization> organizations = new ArrayList<>();
            BitbucketWorkspaceListResponse workspaceListResponse = response.getBody();
            if (workspaceListResponse != null && workspaceListResponse.getValues() != null) {
                for (BitbucketWorkspaceResponse workspaceResponse : workspaceListResponse.getValues()) {
                    organizations.add(new Organization(
                            workspaceResponse.getUuid(),
                            workspaceResponse.getName(),
                            workspaceResponse.getLinks().getHtml().getHref()
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
            HttpHeaders headers = createBitbucketHeaders();
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
                try {
                    ResponseEntity<BitbucketWorkspaceResponse> response = restTemplate.exchange(
                            baseUrl + "/workspaces/" + identifier,
                            HttpMethod.GET,
                            entity,
                            BitbucketWorkspaceResponse.class
                    );

                    BitbucketWorkspaceResponse workspaceResponse = response.getBody();
                    if (workspaceResponse != null) {
                        String htmlUrl = null;
                        if (workspaceResponse.getLinks() != null && workspaceResponse.getLinks().getHtml() != null) {
                            htmlUrl = workspaceResponse.getLinks().getHtml().getHref();
                        }

                        return Optional.of(new Organization(
                                workspaceResponse.getUuid(),
                                workspaceResponse.getName(),
                                htmlUrl
                        ));
                    }
                } catch (Exception e) {
                    // Try next identifier
                    continue;
                }
            }
        } catch (Exception e) {
            // All attempts failed
        }

        return Optional.empty();
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable page) {
        try {
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = baseUrl + "/workspaces/" + org.getName() + "/members?page=" +
                    (page.getPageNumber() + 1) + "&pagelen=" + page.getPageSize();

            ResponseEntity<BitbucketUserListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BitbucketUserListResponse.class
            );

            List<User> members = new ArrayList<>();
            BitbucketUserListResponse userListResponse = response.getBody();
            if (userListResponse != null && userListResponse.getValues() != null) {
                for (BitbucketWorkspaceMembership membership : userListResponse.getValues()) {
                    if (membership.getUser() != null) {
                        BitbucketUserResponse userResponse = membership.getUser();
                        String avatarUrl = null;
                        String htmlUrl = null;

                        if (userResponse.getLinks() != null) {
                            if (userResponse.getLinks().getAvatar() != null) {
                                avatarUrl = userResponse.getLinks().getAvatar().getHref();
                            }
                            if (userResponse.getLinks().getHtml() != null) {
                                htmlUrl = userResponse.getLinks().getHtml().getHref();
                            }
                        }

                        // Use nickname as username, fallback to accountId if nickname is null
                        String username = userResponse.getNickname();
                        if (username == null || username.isEmpty()) {
                            username = userResponse.getAccountId();
                        }

                        members.add(new User(
                                userResponse.getUuid(),
                                username,
                                userResponse.getDisplayName(),
                                avatarUrl,
                                htmlUrl
                        ));
                    }
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
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url;
            if (org != null) {
                url = baseUrl + "/repositories/" + org.getName() + "?page=" +
                        (page.getPageNumber() + 1) + "&pagelen=" + page.getPageSize();
            } else {
                // For user repositories, we need to use the user's username
                // If the user doesn't have a workspace, we'll get an empty result
                url = baseUrl + "/repositories/" + usr.getUsername() + "?page=" +
                        (page.getPageNumber() + 1) + "&pagelen=" + page.getPageSize();
            }

            ResponseEntity<BitbucketRepositoryListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BitbucketRepositoryListResponse.class
            );

            List<Repository> repositories = new ArrayList<>();
            BitbucketRepositoryListResponse repoListResponse = response.getBody();
            if (repoListResponse != null && repoListResponse.getValues() != null) {
                for (BitbucketRepositoryResponse repoResponse : repoListResponse.getValues()) {
                    repositories.add(new Repository(
                            repoResponse.getUuid(),
                            repoResponse.getName(),
                            repoResponse.getDescription(),
                            repoResponse.getLinks().getClone().stream()
                                    .filter(link -> link.getName().equals("https"))
                                    .findFirst()
                                    .map(BitbucketLink::getHref)
                                    .orElse(null),
                            repoResponse.getLinks().getClone().stream()
                                    .filter(link -> link.getName().equals("ssh"))
                                    .findFirst()
                                    .map(BitbucketLink::getHref)
                                    .orElse(null),
                            repoResponse.getMainbranch().getName(),
                            org != null ? OwnerType.ORGANIZATION :
                                    OwnerType.ACCOUNT,
                            repoResponse.getOwner().getUuid(),
                            repoResponse.getIsPrivate() ? Visibility.PRIVATE :
                                    Visibility.PUBLIC
                    ));
                }
            }

            return new PageImpl<>(repositories, page, repositories.size());
        } catch (Exception e) {
            // If it's a 404 error for user repositories, it might mean the user doesn't have a workspace
            if (e.getMessage() != null && e.getMessage().contains("404") && usr != null) {
                // Return empty page for user repositories if user doesn't have a workspace
                return new PageImpl<>(new ArrayList<>(), page, 0);
            }
            throw new RuntimeException("Failed to list repositories", e);
        }
    }

    @Override
    public Optional<Repository> getRepository(String id) {
        try {
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<BitbucketRepositoryResponse> response = restTemplate.exchange(
                    baseUrl + "/repositories/" + id,
                    HttpMethod.GET,
                    entity,
                    BitbucketRepositoryResponse.class
            );

            BitbucketRepositoryResponse repoResponse = response.getBody();
            if (repoResponse != null) {
                return Optional.of(new Repository(
                        repoResponse.getUuid(),
                        repoResponse.getName(),
                        repoResponse.getDescription(),
                        repoResponse.getLinks().getClone().stream()
                                .filter(link -> link.getName().equals("https"))
                                .findFirst()
                                .map(BitbucketLink::getHref)
                                .orElse(null),
                        repoResponse.getLinks().getClone().stream()
                                .filter(link -> link.getName().equals("ssh"))
                                .findFirst()
                                .map(BitbucketLink::getHref)
                                .orElse(null),
                        repoResponse.getMainbranch().getName(),
                        OwnerType.ACCOUNT, // Default to ACCOUNT
                        repoResponse.getOwner().getUuid(),
                        repoResponse.getIsPrivate() ? Visibility.PRIVATE :
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
            HttpHeaders headers = createBitbucketHeaders();
            headers.set("Content-Type", "application/json");

            // Determine workspace based on owner type
            String workspace;
            if (repositoryToCreate.getOwnerType() == OwnerType.ORGANIZATION) {
                // For organization repositories, use the organization ID as workspace
                workspace = repositoryToCreate.getOwnerId();
            } else {
                // For user repositories, we need to get the user's username from the ownerId (UUID)
                // Fetch the user information to get their username
                User user = getUserByUuid(repositoryToCreate.getOwnerId());
                workspace = user.getUsername();
            }

            if (workspace == null || workspace.isEmpty()) {
                throw new IllegalArgumentException("Owner ID (workspace) is required for Bitbucket repository creation");
            }

            // Create request payload
            BitbucketCreateRepositoryRequest request = new BitbucketCreateRepositoryRequest();
            request.scm = "git";
            request.isPrivate = repositoryToCreate.getVisibility() == Visibility.PRIVATE;
            request.name = repositoryToCreate.getName();
            request.description = repositoryToCreate.getDescription();

            HttpEntity<BitbucketCreateRepositoryRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BitbucketRepositoryResponse> response = restTemplate.exchange(
                    baseUrl + "/repositories/" + workspace + "/" + repositoryToCreate.getName(),
                    HttpMethod.POST,
                    entity,
                    BitbucketRepositoryResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                BitbucketRepositoryResponse repoResponse = response.getBody();
                return new Repository(
                        repoResponse.getUuid(),
                        repoResponse.getName(),
                        repoResponse.getDescription(),
                        repoResponse.getLinks().getClone().stream()
                                .filter(link -> link.getName().equals("https"))
                                .findFirst()
                                .map(BitbucketLink::getHref)
                                .orElse(null),
                        repoResponse.getLinks().getClone().stream()
                                .filter(link -> link.getName().equals("ssh"))
                                .findFirst()
                                .map(BitbucketLink::getHref)
                                .orElse(null),
                        repoResponse.getMainbranch().getName(),
                        repositoryToCreate.getOwnerType(), // Use the input owner type
                        repoResponse.getOwner().getUuid(),
                        repoResponse.getIsPrivate() ? Visibility.PRIVATE :
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
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Determine workspace from org or user
            String workspace = (org != null) ? org.getName() : usr.getUsername();
            String repoSlug = repository.getName();
            
            String url = baseUrl + "/repositories/" + workspace + "/" + repoSlug + "/commits?page=" +
                    (page.getPageNumber() + 1) + "&pagelen=" + page.getPageSize();

            ResponseEntity<BitbucketCommitListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BitbucketCommitListResponse.class
            );

            List<Commit> commits = new ArrayList<>();
            BitbucketCommitListResponse commitListResponse = response.getBody();
            if (commitListResponse != null && commitListResponse.getValues() != null) {
                for (BitbucketCommitResponse commitResponse : commitListResponse.getValues()) {
                    // Handle case where author user might be null
                    String authorId = null;
                    if (commitResponse.getAuthor() != null && 
                        commitResponse.getAuthor().getUser() != null) {
                        authorId = commitResponse.getAuthor().getUser().getAccountId();
                    }
                    
                    commits.add(new Commit(
                            commitResponse.getHash(),
                            commitResponse.getMessage(),
                            authorId,
                            commitResponse.getDate()
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
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Determine workspace from org or user
            String workspace = (org != null) ? org.getName() : usr.getUsername();
            String repoSlug = repository.getName();
            
            String url = baseUrl + "/repositories/" + workspace + "/" + repoSlug + "/refs/branches?page=" +
                    (page.getPageNumber() + 1) + "&pagelen=" + page.getPageSize();

            ResponseEntity<BitbucketBranchListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BitbucketBranchListResponse.class
            );

            List<Branch> branches = new ArrayList<>();
            BitbucketBranchListResponse branchListResponse = response.getBody();
            if (branchListResponse != null && branchListResponse.getValues() != null) {
                for (BitbucketBranchResponse branchResponse : branchListResponse.getValues()) {
                    Branch branch = new Branch(
                            branchResponse.getName(),
                            branchResponse.getTarget().getHash()
                    );
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
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Determine workspace from org or user
            String workspace = (org != null) ? org.getName() : usr.getUsername();
            String repoSlug = repository.getName();
            
            String url = baseUrl + "/repositories/" + workspace + "/" + repoSlug + "/refs/tags?page=" +
                    (page.getPageNumber() + 1) + "&pagelen=" + page.getPageSize();

            ResponseEntity<BitbucketTagListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BitbucketTagListResponse.class
            );

            List<Tag> tags = new ArrayList<>();
            BitbucketTagListResponse tagListResponse = response.getBody();
            if (tagListResponse != null && tagListResponse.getValues() != null) {
                for (BitbucketTagResponse tagResponse : tagListResponse.getValues()) {
                    Tag tag = new Tag(
                            tagResponse.getName(),
                            tagResponse.getTarget().getHash()
                    );
                    tags.add(tag);
                }
            }

            return new PageImpl<>(tags, page, tags.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list tags", e);
        }
    }


    /**
     * Get user information by UUID (Atlassian Account ID) from Bitbucket API
     *
     * @param uuid The user UUID (Atlassian Account ID) to look up
     * @return User object with user information
     */
    private User getUserByUuid(String uuid) {
        try {
            HttpHeaders headers = createBitbucketHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<BitbucketUserResponse> response = restTemplate.exchange(
                    baseUrl + "/users/" + uuid,
                    HttpMethod.GET,
                    entity,
                    BitbucketUserResponse.class
            );

            BitbucketUserResponse userResponse = response.getBody();
            if (userResponse != null) {
                String avatarUrl = null;
                String htmlUrl = null;

                if (userResponse.getLinks() != null) {
                    if (userResponse.getLinks().getAvatar() != null) {
                        avatarUrl = userResponse.getLinks().getAvatar().getHref();
                    }
                    if (userResponse.getLinks().getHtml() != null) {
                        htmlUrl = userResponse.getLinks().getHtml().getHref();
                    }
                }

                return new User(
                        userResponse.getUuid(),
                        userResponse.getUsername(),
                        userResponse.getDisplayName(),
                        avatarUrl,
                        htmlUrl
                );
            }

            throw new RuntimeException("User not found: " + uuid);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user by UUID: " + e.getMessage(), e);
        }
    }

    /**
     * Create Bitbucket-specific HTTP headers for authentication.
     * Bitbucket uses basic authentication with email as username and API token as password.
     * Additional headers are included for better API compatibility and user identification.
     */
    private HttpHeaders createBitbucketHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (patCredential != null) {
            headers.setBasicAuth(patCredential.getUsername(), patCredential.getToken());
        } else {
            //TODO support Oauth 2 and token caching
            throw new RuntimeException("Invalid auth method");
        }


        // Add common headers for Bitbucket API
        headers.set("Accept", "application/json");
        // Add content type for requests that include body
        headers.set("Content-Type", "application/json");

        headers.set("User-Agent", "GitProviderDemo/1.0");//TODO

        // Add username information for better API compatibility
        headers.set("X-Atlassian-Username", patCredential.getUsername());


        return headers;
    }

    // Bitbucket API response classes
    private static class BitbucketUserListResponse {
        @JsonProperty("values")
        private List<BitbucketWorkspaceMembership> values;

        public List<BitbucketWorkspaceMembership> getValues() {
            return values;
        }

        public void setValues(List<BitbucketWorkspaceMembership> values) {
            this.values = values;
        }
    }

    private static class BitbucketWorkspaceMembership {
        @JsonProperty("user")
        private BitbucketUserResponse user;

        public BitbucketUserResponse getUser() {
            return user;
        }

        public void setUser(BitbucketUserResponse user) {
            this.user = user;
        }
    }

    private static class BitbucketUserResponse {
        @JsonProperty("uuid")
        private String uuid;
        @JsonProperty("username")
        private String username;
        @JsonProperty("account_id")
        private String accountId;
        @JsonProperty("nickname")
        private String nickname;
        @JsonProperty("display_name")
        private String displayName;
        @JsonProperty("links")
        private BitbucketLinks links;

        // Getters and setters
        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public BitbucketLinks getLinks() {
            return links;
        }

        public void setLinks(BitbucketLinks links) {
            this.links = links;
        }
    }

    private static class BitbucketWorkspaceResponse {
        @JsonProperty("uuid")
        private String uuid;
        @JsonProperty("name")
        private String name;
        @JsonProperty("links")
        private BitbucketLinks links;

        // Getters and setters
        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BitbucketLinks getLinks() {
            return links;
        }

        public void setLinks(BitbucketLinks links) {
            this.links = links;
        }
    }

    private static class BitbucketRepositoryResponse {
        @JsonProperty("uuid")
        private String uuid;
        @JsonProperty("name")
        private String name;
        @JsonProperty("description")
        private String description;
        @JsonProperty("is_private")
        private boolean isPrivate;
        @JsonProperty("mainbranch")
        private BitbucketMainBranch mainbranch;
        @JsonProperty("owner")
        private BitbucketUserResponse owner;
        @JsonProperty("links")
        private BitbucketLinks links;

        // Getters and setters
        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
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

        public boolean getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
        }

        public BitbucketMainBranch getMainbranch() {
            return mainbranch;
        }

        public void setMainbranch(BitbucketMainBranch mainbranch) {
            this.mainbranch = mainbranch;
        }

        public BitbucketUserResponse getOwner() {
            return owner;
        }

        public void setOwner(BitbucketUserResponse owner) {
            this.owner = owner;
        }

        public BitbucketLinks getLinks() {
            return links;
        }

        public void setLinks(BitbucketLinks links) {
            this.links = links;
        }
    }

    private static class BitbucketMainBranch {
        @JsonProperty("name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class BitbucketLinks {
        @JsonProperty("avatar")
        private BitbucketLink avatar;
        @JsonProperty("html")
        private BitbucketLink html;
        @JsonProperty("clone")
        private List<BitbucketLink> clone;

        public BitbucketLink getAvatar() {
            return avatar;
        }

        public void setAvatar(BitbucketLink avatar) {
            this.avatar = avatar;
        }

        public BitbucketLink getHtml() {
            return html;
        }

        public void setHtml(BitbucketLink html) {
            this.html = html;
        }

        public List<BitbucketLink> getClone() {
            return clone;
        }

        public void setClone(List<BitbucketLink> clone) {
            this.clone = clone;
        }
    }

    private static class BitbucketLink {
        @JsonProperty("name")
        private String name;
        @JsonProperty("href")
        private String href;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }

    private static class BitbucketWorkspaceListResponse {
        private List<BitbucketWorkspaceResponse> values;

        public List<BitbucketWorkspaceResponse> getValues() {
            return values;
        }

        public void setValues(List<BitbucketWorkspaceResponse> values) {
            this.values = values;
        }
    }


    private static class BitbucketRepositoryListResponse {
        private List<BitbucketRepositoryResponse> values;

        public List<BitbucketRepositoryResponse> getValues() {
            return values;
        }

        public void setValues(List<BitbucketRepositoryResponse> values) {
            this.values = values;
        }
    }

    private static class BitbucketCreateRepositoryRequest {
        @JsonProperty("scm")
        private String scm;

        @JsonProperty("is_private")
        private boolean isPrivate;

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        public String getScm() {
            return scm;
        }

        public void setScm(String scm) {
            this.scm = scm;
        }

        public boolean isPrivate() {
            return isPrivate;
        }

        public void setPrivate(boolean aPrivate) {
            isPrivate = aPrivate;
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
    }

    // Response classes for Bitbucket API

    public static class BitbucketCommitListResponse {
        private List<BitbucketCommitResponse> values;

        public List<BitbucketCommitResponse> getValues() { return values; }
        public void setValues(List<BitbucketCommitResponse> values) { this.values = values; }
    }

    public static class BitbucketCommitResponse {
        private String hash;
        private String message;
        private BitbucketCommitAuthor author;
        private java.util.Date date;

        public String getHash() { return hash; }
        public void setHash(String hash) { this.hash = hash; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public BitbucketCommitAuthor getAuthor() { return author; }
        public void setAuthor(BitbucketCommitAuthor author) { this.author = author; }
        public java.util.Date getDate() { return date; }
        public void setDate(java.util.Date date) { this.date = date; }
    }

    public static class BitbucketCommitAuthor {
        private BitbucketUser user;

        public BitbucketUser getUser() { return user; }
        public void setUser(BitbucketUser user) { this.user = user; }
    }

    public static class BitbucketUser {
        private String displayName;
        private String accountId;

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
    }

    public static class BitbucketBranchListResponse {
        private List<BitbucketBranchResponse> values;

        public List<BitbucketBranchResponse> getValues() { return values; }
        public void setValues(List<BitbucketBranchResponse> values) { this.values = values; }
    }

    public static class BitbucketBranchResponse {
        private String name;
        private BitbucketBranchTarget target;
        private BitbucketBranchLinks links;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BitbucketBranchTarget getTarget() { return target; }
        public void setTarget(BitbucketBranchTarget target) { this.target = target; }
        public BitbucketBranchLinks getLinks() { return links; }
        public void setLinks(BitbucketBranchLinks links) { this.links = links; }
    }

    public static class BitbucketBranchTarget {
        private String hash;

        public String getHash() { return hash; }
        public void setHash(String hash) { this.hash = hash; }
    }

    public static class BitbucketBranchLinks {
        private BitbucketBranchHtml html;

        public BitbucketBranchHtml getHtml() { return html; }
        public void setHtml(BitbucketBranchHtml html) { this.html = html; }
    }

    public static class BitbucketBranchHtml {
        private String href;

        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }
    }

    public static class BitbucketTagListResponse {
        private List<BitbucketTagResponse> values;

        public List<BitbucketTagResponse> getValues() { return values; }
        public void setValues(List<BitbucketTagResponse> values) { this.values = values; }
    }

    public static class BitbucketTagResponse {
        private String name;
        private String message;
        private BitbucketTagTarget target;
        private BitbucketTagLinks links;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public BitbucketTagTarget getTarget() { return target; }
        public void setTarget(BitbucketTagTarget target) { this.target = target; }
        public BitbucketTagLinks getLinks() { return links; }
        public void setLinks(BitbucketTagLinks links) { this.links = links; }
    }

    public static class BitbucketTagTarget {
        private String hash;

        public String getHash() { return hash; }
        public void setHash(String hash) { this.hash = hash; }
    }

    public static class BitbucketTagLinks {
        private BitbucketTagHtml html;

        public BitbucketTagHtml getHtml() { return html; }
        public void setHtml(BitbucketTagHtml html) { this.html = html; }
    }

    public static class BitbucketTagHtml {
        private String href;

        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }
    }
}

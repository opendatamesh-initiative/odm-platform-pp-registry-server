package org.opendatamesh.platform.pp.registry.githandler.provider.azure;

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
    private final PatCredential patCredential;

    public AzureDevOpsProvider(String baseUrl, RestTemplate restTemplate, PatCredential patCredential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://dev.azure.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.patCredential = patCredential;

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
            ResponseEntity<AzureUserResponse> response = restTemplate.exchange(
                    baseUrl + "/_apis/connectionData?api-version=7.1-preview.1",
                    HttpMethod.GET,
                    entity,
                    AzureUserResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Connection successful - we can access Azure DevOps with our credentials
                return;
            } else {
                throw new RuntimeException("Failed to authenticate with Azure DevOps API");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Azure DevOps: " + e.getMessage(), e);
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First get the connection data to get the user ID
            ResponseEntity<AzureUserResponse> response = restTemplate.exchange(
                    baseUrl + "/_apis/connectionData?api-version=7.1-preview.1",
                    HttpMethod.GET,
                    entity,
                    AzureUserResponse.class
            );

            AzureUserResponse userResponse = response.getBody();
            if (userResponse != null && userResponse.getAuthenticatedUser() != null) {
                AzureUser authenticatedUser = userResponse.getAuthenticatedUser();

                // Debug: Print the actual user data from connectionData
                System.out.println("   Debug: ConnectionData - ID: " + authenticatedUser.getId());
                System.out.println("   Debug: ConnectionData - SubjectDescriptor: " + authenticatedUser.getSubjectDescriptor());
                System.out.println("   Debug: ConnectionData - ProviderDisplayName: " + authenticatedUser.getProviderDisplayName());
                System.out.println("   Debug: ConnectionData - Descriptor: " + authenticatedUser.getDescriptor());
                if (authenticatedUser.getProperties() != null && authenticatedUser.getProperties().getAccount() != null) {
                    System.out.println("   Debug: ConnectionData - Account: " + authenticatedUser.getProperties().getAccount().getValue());
                }

                // Extract email from descriptor if available
                String email = null;
                if (authenticatedUser.getDescriptor() != null && authenticatedUser.getDescriptor().contains("\\")) {
                    String[] parts = authenticatedUser.getDescriptor().split("\\\\");
                    if (parts.length > 1) {
                        email = parts[1];
                    }
                }

                // Use the providerDisplayName as the display name, fallback to email or subject descriptor
                String displayName = authenticatedUser.getProviderDisplayName();
                if (displayName == null || displayName.trim().isEmpty()) {
                    displayName = email != null ? email : authenticatedUser.getSubjectDescriptor();
                }

                // Use email as username if available, otherwise use subject descriptor
                String username = email != null ? email : authenticatedUser.getSubjectDescriptor();

                return new User(
                        authenticatedUser.getId(),
                        username,
                        displayName,
                        null, // No avatar URL available from connectionData
                        baseUrl + "/_usersSettings/about"
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current user", e);
        }

        throw new RuntimeException("Failed to get current user");
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
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String apiUrl = baseUrl + "/_apis/projects?api-version=7.1&$top=" + page.getPageSize() +
                    "&$skip=" + (page.getPageNumber() * page.getPageSize());

            ResponseEntity<AzureProjectListResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    AzureProjectListResponse.class
            );

            // For simplicity, we'll return the current user as the only member
            // In a real implementation, you'd need to call the teams/members API
            List<User> members = new ArrayList<>();
            members.add(getCurrentUser());

            return new PageImpl<>(members, page, members.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list organization members", e);
        }
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, Pageable page) {
        try {
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First get projects, then get repositories from each project
            String projectsUrl = baseUrl + "/_apis/projects?api-version=7.1";
            ResponseEntity<AzureProjectListResponse> projectsResponse = restTemplate.exchange(
                    projectsUrl,
                    HttpMethod.GET,
                    entity,
                    AzureProjectListResponse.class
            );

            List<Repository> repositories = new ArrayList<>();
            AzureProjectListResponse projectsListResponse = projectsResponse.getBody();
            if (projectsListResponse != null && projectsListResponse.getValue() != null) {
                for (AzureProject project : projectsListResponse.getValue()) {
                    String reposUrl = baseUrl + "/" + project.getName() + "/_apis/git/repositories?api-version=7.1";
                    ResponseEntity<AzureRepositoryListResponse> reposResponse = restTemplate.exchange(
                            reposUrl,
                            HttpMethod.GET,
                            entity,
                            AzureRepositoryListResponse.class
                    );

                    AzureRepositoryListResponse reposListResponse = reposResponse.getBody();
                    if (reposListResponse != null && reposListResponse.getValue() != null) {
                        for (AzureRepository repo : reposListResponse.getValue()) {
                            repositories.add(new Repository(
                                    repo.getId(),
                                    repo.getName(),
                                    repo.getDescription(),
                                    repo.getRemoteUrl(),
                                    null, // SSH URL not always available
                                    repo.getDefaultBranch(),
                                    OwnerType.ORGANIZATION,
                                    project.getId(),
                                    Visibility.PRIVATE // Azure DevOps repos are typically private
                            ));
                        }
                    }
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
            HttpHeaders headers = createAzureDevOpsHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First get all projects to find the repository
            String projectsUrl = baseUrl + "/_apis/projects?api-version=7.1";
            ResponseEntity<AzureProjectListResponse> projectsResponse = restTemplate.exchange(
                    projectsUrl,
                    HttpMethod.GET,
                    entity,
                    AzureProjectListResponse.class
            );

            AzureProjectListResponse projectsListResponse = projectsResponse.getBody();
            if (projectsListResponse != null && projectsListResponse.getValue() != null) {
                for (AzureProject project : projectsListResponse.getValue()) {
                    String repoUrl = baseUrl + "/" + project.getName() + "/_apis/git/repositories/" + id + "?api-version=7.1";
                    try {
                        ResponseEntity<AzureRepository> repoResponse = restTemplate.exchange(
                                repoUrl,
                                HttpMethod.GET,
                                entity,
                                AzureRepository.class
                        );

                        AzureRepository repo = repoResponse.getBody();
                        if (repo != null) {
                            return Optional.of(new Repository(
                                    repo.getId(),
                                    repo.getName(),
                                    repo.getDescription(),
                                    repo.getRemoteUrl(),
                                    null,
                                    repo.getDefaultBranch(),
                                    OwnerType.ORGANIZATION,
                                    project.getId(),
                                    Visibility.PRIVATE
                            ));
                        }
                    } catch (Exception e) {
                        // Repository not found in this project, continue searching
                    }
                }
            }
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

            // For Azure DevOps, we need to specify a project
            // We'll use the ownerId as the project name, or default to a project
            String projectName = repositoryToCreate.getOwnerId();
            if (projectName == null || projectName.isEmpty()) {
                // Get the first available project as default
                String projectsUrl = baseUrl + "/_apis/projects?api-version=7.1";
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<AzureProjectListResponse> projectsResponse = restTemplate.exchange(
                        projectsUrl,
                        HttpMethod.GET,
                        entity,
                        AzureProjectListResponse.class
                );

                AzureProjectListResponse projectsListResponse = projectsResponse.getBody();
                if (projectsListResponse != null && projectsListResponse.getValue() != null && !projectsListResponse.getValue().isEmpty()) {
                    projectName = projectsListResponse.getValue().get(0).getName();
                } else {
                    throw new IllegalArgumentException("No projects found in Azure DevOps organization");
                }
            }

            // Create request payload
            AzureCreateRepositoryRequest request = new AzureCreateRepositoryRequest();
            request.name = repositoryToCreate.getName();
            request.project = new AzureProjectReference();
            request.project.id = projectName;

            HttpEntity<AzureCreateRepositoryRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AzureRepository> response = restTemplate.exchange(
                    baseUrl + "/" + projectName + "/_apis/git/repositories?api-version=7.1",
                    HttpMethod.POST,
                    entity,
                    AzureRepository.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AzureRepository repo = response.getBody();
                return new Repository(
                        repo.getId(),
                        repo.getName(),
                        repo.getDescription(),
                        repo.getRemoteUrl(),
                        null,
                        repo.getDefaultBranch(),
                        OwnerType.ORGANIZATION,
                        projectName,
                        Visibility.PRIVATE
                );
            }

            throw new RuntimeException("Failed to create repository. Status: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create repository: " + e.getMessage(), e);
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
            // For Azure DevOps, we need to use basic auth with PAT as password
            // Azure DevOps uses username:token format for basic auth
            headers.set("username", "dummy"); // Azure DevOps doesn't use username for PAT auth
            headers.set("password", patCredential.getToken());
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
        HttpHeaders headers = new HttpHeaders();

        if (patCredential != null) {
            headers.setBearerAuth(patCredential.getToken());
        } else {
            throw new IllegalStateException("PAT credential is required for Azure DevOps authentication");
        }

        // Add common headers for Azure DevOps API
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "GitProviderDemo/1.0");

        return headers;
    }

    // Azure DevOps API response classes
    private static class AzureUserResponse {
        private AzureUser authenticatedUser;

        public AzureUser getAuthenticatedUser() {
            return authenticatedUser;
        }

        public void setAuthenticatedUser(AzureUser authenticatedUser) {
            this.authenticatedUser = authenticatedUser;
        }
    }

    private static class AzureUser {
        private String id;
        private String subjectDescriptor;
        private String displayName;
        private String imageUrl;
        private String url;
        private String providerDisplayName;
        private String descriptor;
        private AzureUserProperties properties;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSubjectDescriptor() {
            return subjectDescriptor;
        }

        public void setSubjectDescriptor(String subjectDescriptor) {
            this.subjectDescriptor = subjectDescriptor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getProviderDisplayName() {
            return providerDisplayName;
        }

        public void setProviderDisplayName(String providerDisplayName) {
            this.providerDisplayName = providerDisplayName;
        }

        public String getDescriptor() {
            return descriptor;
        }

        public void setDescriptor(String descriptor) {
            this.descriptor = descriptor;
        }

        public AzureUserProperties getProperties() {
            return properties;
        }

        public void setProperties(AzureUserProperties properties) {
            this.properties = properties;
        }
    }

    private static class AzureUserProperties {
        private AzureUserAccount account;

        public AzureUserAccount getAccount() {
            return account;
        }

        public void setAccount(AzureUserAccount account) {
            this.account = account;
        }
    }

    private static class AzureUserAccount {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class AzureUsersResponse {
        private List<AzureUserInfo> value;

        public List<AzureUserInfo> getValue() {
            return value;
        }

        public void setValue(List<AzureUserInfo> value) {
            this.value = value;
        }
    }

    private static class AzureUserInfo {
        private String id;
        private String displayName;
        private String uniqueName;
        private String imageUrl;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getUniqueName() {
            return uniqueName;
        }

        public void setUniqueName(String uniqueName) {
            this.uniqueName = uniqueName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    private static class AzureProjectListResponse {
        private List<AzureProject> value;

        public List<AzureProject> getValue() {
            return value;
        }

        public void setValue(List<AzureProject> value) {
            this.value = value;
        }
    }

    private static class AzureProject {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class AzureRepositoryListResponse {
        private List<AzureRepository> value;

        public List<AzureRepository> getValue() {
            return value;
        }

        public void setValue(List<AzureRepository> value) {
            this.value = value;
        }
    }

    private static class AzureRepository {
        private String id;
        private String name;
        private String description;
        private String remoteUrl;
        private String defaultBranch;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
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

        public String getRemoteUrl() {
            return remoteUrl;
        }

        public void setRemoteUrl(String remoteUrl) {
            this.remoteUrl = remoteUrl;
        }

        public String getDefaultBranch() {
            return defaultBranch;
        }

        public void setDefaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
        }
    }

    private static class AzureCreateRepositoryRequest {
        @JsonProperty("name")
        private String name;

        @JsonProperty("project")
        private AzureProjectReference project;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AzureProjectReference getProject() {
            return project;
        }

        public void setProject(AzureProjectReference project) {
            this.project = project;
        }
    }

    private static class AzureProjectReference {
        @JsonProperty("id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}

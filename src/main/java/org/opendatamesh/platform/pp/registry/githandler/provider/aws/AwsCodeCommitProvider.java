package org.opendatamesh.platform.pp.registry.githandler.provider.aws;

import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.AwsCredential;
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
 * AWS CodeCommit provider implementation
 * 
 * Supported authentication methods:
 * - AWS Signature V4 (IAM credentials) - ✅ Only supported method
 * - SSH Keys - ✅ (Git operations only, tied to IAM user)
 */
public class AwsCodeCommitProvider implements GitProvider {
    
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final AwsCredential awsCredential;

    public AwsCodeCommitProvider(String baseUrl, RestTemplate restTemplate, AwsCredential awsCredential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://codecommit.us-east-1.amazonaws.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.awsCredential = awsCredential;
    }

    @Override
    public void checkConnection() {
        try {
            String endpoint = "https://codecommit." + awsCredential.getRegion() + ".amazonaws.com/";
            HttpHeaders headers = createAwsHeaders();
            headers.set("X-Amz-Target", "CodeCommit_20150413.ListRepositories");
            
            // Create a minimal request body for ListRepositories
            String requestBody = "{}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<AwsListRepositoriesResponse> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                AwsListRepositoriesResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // Connection successful - we can access AWS CodeCommit with our credentials
                return;
            } else {
                throw new RuntimeException("Failed to authenticate with AWS CodeCommit API");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to AWS CodeCommit: " + e.getMessage(), e);
        }
    }

    @Override
    public User getCurrentUser() {
        // AWS CodeCommit doesn't have a direct "current user" concept
        // We'll return a user based on the access key ID
        return new User(
            awsCredential.getAwsAccessKeyId(),
            awsCredential.getAwsAccessKeyId(),
            "AWS User",
            null,
            null
        );
    }

    @Override
    public Page<Organization> listOrganizations(Pageable page) {
        // AWS CodeCommit doesn't have organizations in the traditional sense
        // We'll return an empty page or create a default "AWS" organization
        List<Organization> organizations = new ArrayList<>();
        organizations.add(new Organization(
            "aws-default",
            "AWS Default",
            "https://console.aws.amazon.com/codesuite/codecommit"
        ));
        
        return new PageImpl<>(organizations, page, organizations.size());
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        if ("aws-default".equals(id)) {
            return Optional.of(new Organization(
                "aws-default",
                "AWS Default",
                "https://console.aws.amazon.com/codesuite/codecommit"
            ));
        }
        
        return Optional.empty();
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable page) {
        // AWS CodeCommit doesn't have traditional organization members
        // Return the current user as the only member
        List<User> members = new ArrayList<>();
        members.add(getCurrentUser());
        
        return new PageImpl<>(members, page, members.size());
    }


    @Override
    public Page<Repository> listRepositories(Organization org, User usr, Pageable page) {
        try {
            String endpoint = "https://codecommit." + awsCredential.getRegion() + ".amazonaws.com/";
            HttpHeaders headers = createAwsHeaders();
            headers.set("X-Amz-Target", "CodeCommit_20150413.ListRepositories");
            
            // Create the request body for ListRepositories
            String requestBody = "{}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<AwsListRepositoriesResponse> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                AwsListRepositoriesResponse.class
            );
            
            List<Repository> repositories = new ArrayList<>();
            AwsListRepositoriesResponse listResponse = response.getBody();
            if (listResponse != null && listResponse.getRepositories() != null) {
                for (AwsRepositoryResponse repoResponse : listResponse.getRepositories()) {
                    repositories.add(new Repository(
                        repoResponse.getRepositoryId(),
                        repoResponse.getRepositoryName(),
                        repoResponse.getRepositoryDescription(),
                        repoResponse.getCloneUrlHttp(),
                        repoResponse.getCloneUrlSsh(),
                        repoResponse.getDefaultBranch(),
                        OwnerType.ACCOUNT,
                        awsCredential.getAwsAccessKeyId(),
                        Visibility.PRIVATE // AWS repos are typically private
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
            String endpoint = "https://codecommit." + awsCredential.getRegion() + ".amazonaws.com/";
            HttpHeaders headers = createAwsHeaders();
            headers.set("X-Amz-Target", "CodeCommit_20150413.GetRepository");
            
            // Create the request body for GetRepository
            String requestBody = "{\"repositoryName\":\"" + id + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<AwsRepositoryResponse> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                AwsRepositoryResponse.class
            );
            
            AwsRepositoryResponse repoResponse = response.getBody();
            if (repoResponse != null) {
                return Optional.of(new Repository(
                    repoResponse.getRepositoryId(),
                    repoResponse.getRepositoryName(),
                    repoResponse.getRepositoryDescription(),
                    repoResponse.getCloneUrlHttp(),
                    repoResponse.getCloneUrlSsh(),
                    repoResponse.getDefaultBranch(),
                    OwnerType.ACCOUNT,
                    awsCredential.getAwsAccessKeyId(),
                    Visibility.PRIVATE
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
            String endpoint = "https://codecommit." + awsCredential.getRegion() + ".amazonaws.com/";
            HttpHeaders headers = createAwsHeaders();
            headers.set("X-Amz-Target", "CodeCommit_20150413.CreateRepository");
            headers.set("Content-Type", "application/x-amz-json-1.1");
            
            // Create the request body for CreateRepository
            String requestBody = String.format(
                "{\"repositoryName\":\"%s\",\"repositoryDescription\":\"%s\"}",
                repositoryToCreate.getName(),
                repositoryToCreate.getDescription() != null ? repositoryToCreate.getDescription() : ""
            );
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<AwsRepositoryResponse> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                AwsRepositoryResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AwsRepositoryResponse repoResponse = response.getBody();
                return new Repository(
                    repoResponse.getRepositoryId(),
                    repoResponse.getRepositoryName(),
                    repoResponse.getRepositoryDescription(),
                    repoResponse.getCloneUrlHttp(),
                    repoResponse.getCloneUrlSsh(),
                    repoResponse.getDefaultBranch(),
                    OwnerType.ACCOUNT,
                    awsCredential.getAwsAccessKeyId(),
                    Visibility.PRIVATE
                );
            }
            
            throw new RuntimeException("Failed to create repository. Status: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create repository: " + e.getMessage(), e);
        }
    }

    // AWS CodeCommit API response classes
    private static class AwsListRepositoriesResponse {
        private List<AwsRepositoryResponse> repositories;

        public List<AwsRepositoryResponse> getRepositories() { return repositories; }
        public void setRepositories(List<AwsRepositoryResponse> repositories) { this.repositories = repositories; }
    }

    private static class AwsRepositoryResponse {
        private String repositoryId;
        private String repositoryName;
        private String repositoryDescription;
        private String cloneUrlHttp;
        private String cloneUrlSsh;
        private String defaultBranch;

        // Getters and setters
        public String getRepositoryId() { return repositoryId; }
        public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
        public String getRepositoryName() { return repositoryName; }
        public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }
        public String getRepositoryDescription() { return repositoryDescription; }
        public void setRepositoryDescription(String repositoryDescription) { this.repositoryDescription = repositoryDescription; }
        public String getCloneUrlHttp() { return cloneUrlHttp; }
        public void setCloneUrlHttp(String cloneUrlHttp) { this.cloneUrlHttp = cloneUrlHttp; }
        public String getCloneUrlSsh() { return cloneUrlSsh; }
        public void setCloneUrlSsh(String cloneUrlSsh) { this.cloneUrlSsh = cloneUrlSsh; }
        public String getDefaultBranch() { return defaultBranch; }
        public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    }

    private HttpHeaders createAwsHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-AWS-Access-Key-Id", this.awsCredential.getAwsAccessKeyId());
        headers.set("X-AWS-Secret-Key", this.awsCredential.getAwsSecretKey());
        if (this.awsCredential.getAwsSessionToken() != null) {
            headers.set("X-AWS-Session-Token", this.awsCredential.getAwsSessionToken());
        }
        headers.set("X-AWS-Region", this.awsCredential.getRegion());
        return headers;
    }
}

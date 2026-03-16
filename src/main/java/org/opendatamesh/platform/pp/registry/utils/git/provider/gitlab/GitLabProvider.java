package org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab;

import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitProviderConfigurationException;
import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitClientException;
import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.utils.git.git.GitOperation;
import org.opendatamesh.platform.pp.registry.utils.git.git.GitOperationImpl;
import org.opendatamesh.platform.pp.registry.utils.git.model.*;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderCredential;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.comparator.GitLabCommitComparator;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.createrepository.GitLabCreateRepositoryMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.createrepository.GitLabCreateRepositoryProjectRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.createrepository.GitLabCreateRepositoryReq;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getcurrentuser.GitLabGetCurrentUserMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getcurrentuser.GitLabGetCurrentUserUserRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getorganization.GitLabGetOrganizationGroupRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getorganization.GitLabGetOrganizationMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getrepository.GitLabGetRepositoryMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getrepository.GitLabGetRepositoryProjectRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listbranches.GitLabListBranchesBranchRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listbranches.GitLabListBranchesMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listcommits.GitLabCompareCommitsRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listcommits.GitLabListCommitsCommitRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listcommits.GitLabListCommitsMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listmembers.GitLabListMembersMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listmembers.GitLabListMembersUserRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listorganizations.GitLabListOrganizationsGroupRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listorganizations.GitLabListOrganizationsMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listrepositories.GitLabListRepositoriesMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listrepositories.GitLabListRepositoriesProjectRes;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listtags.GitLabListTagsMapper;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listtags.GitLabListTagsTagRes;
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
    private final GitProviderCredential credential;

    public GitLabProvider(String baseUrl, RestTemplate restTemplate, GitProviderCredential credential) throws GitProviderConfigurationException {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl is required and cannot be null or empty");
        }
        this.baseUrl = baseUrl.trim();
        this.restTemplate = restTemplate;
        this.credential = credential;
    }

    @Override
    public User getCurrentUser() {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
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

            throw new GitClientException(404, "Failed to get current user: response body is null");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to get current user: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to get current user: " + e.getMessage());
        }
    }

    @Override
    public Page<Organization> listOrganizations(Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/api/v4/groups?page={page}&per_page={perPage}&owned={owned}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());
            uriVariables.put("owned", "true");

            ResponseEntity<GitLabListOrganizationsGroupRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitLabListOrganizationsGroupRes[].class,
                    uriVariables
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
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to list organizations: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to list organizations: " + e.getMessage());
        }
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/api/v4/groups/{id}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("id", id);

            ResponseEntity<GitLabGetOrganizationGroupRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitLabGetOrganizationGroupRes.class,
                    uriVariables
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
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to get organization: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to get organization: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/api/v4/groups/{groupId}/members?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("groupId", org.getId());
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            ResponseEntity<GitLabListMembersUserRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitLabListMembersUserRes[].class,
                    uriVariables
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
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to list organization members: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to list organization members: " + e.getMessage());
        }
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, MultiValueMap<String, String> parameters, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate;
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            if (org != null) {
                uriTemplate = baseUrl + "/api/v4/groups/{groupId}/projects?page={page}&per_page={perPage}";
                uriVariables.put("groupId", org.getId());
            } else {
                uriTemplate = baseUrl + "/api/v4/users/{userId}/projects?page={page}&per_page={perPage}";
                uriVariables.put("userId", usr.getId());
            }

            ResponseEntity<GitLabListRepositoriesProjectRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitLabListRepositoriesProjectRes[].class,
                    uriVariables
            );

            List<Repository> repositories = new ArrayList<>();
            GitLabListRepositoriesProjectRes[] projectResponses = response.getBody();
            if (projectResponses != null) {
                RepositoryOwnerType ownerType = org != null ? RepositoryOwnerType.ORGANIZATION
                        : RepositoryOwnerType.ACCOUNT;
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
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to list repositories: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to list repositories: " + e.getMessage());
        }
    }

    @Override
    public Optional<Repository> getRepository(String id, String ownerId) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uriTemplate = baseUrl + "/api/v4/projects/{id}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("id", id);

            ResponseEntity<GitLabGetRepositoryProjectRes> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitLabGetRepositoryProjectRes.class,
                    uriVariables
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
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to get repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to get repository: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Repository createRepository(Repository repositoryToCreate) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
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

            throw new GitClientException(response.getStatusCode().value(),
                    "Failed to create repository. Status: " + response.getStatusCode());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to create repository: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to create repository: " + e.getMessage());
        }
    }

    @Override
    public Page<Commit> listCommits(Repository repository, CommitListFilter filter, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String projectId = repository.getId();

            if (filter instanceof CommitListRangeFilter r) {
                String uriTemplate = baseUrl + "/api/v4/projects/{projectId}/repository/compare?from={from}&to={to}";
                Map<String, Object> uriVariables = constructUriVariablesListCommitsCompare(projectId, r.from().getRefValue(), r.to().getRefValue());
                ResponseEntity<GitLabCompareCommitsRes> response = callApiListCommitsCompare(uriTemplate, entity, uriVariables);
                List<Commit> commits = mappingListCommitsCompareToInternalModel(response);
                commits.sort(new GitLabCommitComparator());
                return new PageImpl<>(commits, page, commits.size());
            }
            if (filter instanceof CommitListSingleBranchFilter s) {
                String uriTemplate = baseUrl + "/api/v4/projects/{projectId}/repository/commits?ref_name={branchName}&page={page}&per_page={perPage}";
                Map<String, Object> uriVariables = constructUriVariablesListCommits(projectId, page);
                uriVariables.put("branchName", s.branch().getRefValue());
                ResponseEntity<GitLabListCommitsCommitRes[]> response = callApiListCommits(uriTemplate, entity, uriVariables);
                List<Commit> commits = mappingListCommitsToInternalModel(response);
                return new PageImpl<>(commits, page, commits.size());
            }
            // NoCommitListFilter: list default branch
            String uriTemplate = baseUrl + "/api/v4/projects/{projectId}/repository/commits?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = constructUriVariablesListCommits(projectId, page);
            ResponseEntity<GitLabListCommitsCommitRes[]> response = callApiListCommits(uriTemplate, entity, uriVariables);
            List<Commit> commits = mappingListCommitsToInternalModel(response);
            return new PageImpl<>(commits, page, commits.size());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new GitProviderAuthenticationException("GitLab authentication failed with provider. Please check your credentials.");
            }
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to list commits: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to list commits: " + e.getMessage());
        }
    }

    @Override
    public Page<Branch> listBranches(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Extract project ID from repository
            String projectId = repository.getId();

            String uriTemplate = baseUrl + "/api/v4/projects/{projectId}/repository/branches?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("projectId", projectId);
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            ResponseEntity<GitLabListBranchesBranchRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitLabListBranchesBranchRes[].class,
                    uriVariables
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
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to list branches: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to list branches: " + e.getMessage());
        }
    }

    @Override
    public Page<Tag> listTags(Repository repository, Pageable page) {
        try {
            HttpHeaders headers = credential.createGitProviderHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Extract project ID from repository
            String projectId = repository.getId();

            String uriTemplate = baseUrl + "/api/v4/projects/{projectId}/repository/tags?page={page}&per_page={perPage}";
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("projectId", projectId);
            uriVariables.put("page", page.getPageNumber() + 1);
            uriVariables.put("perPage", page.getPageSize());

            ResponseEntity<GitLabListTagsTagRes[]> response = restTemplate.exchange(
                    uriTemplate,
                    HttpMethod.GET,
                    entity,
                    GitLabListTagsTagRes[].class,
                    uriVariables
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
            throw new GitClientException(e.getStatusCode().value(),
                    "GitLab request failed to list tags: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new GitClientException(500, "GitLab request failed to list tags: " + e.getMessage());
        }
    }

    @Override
    public GitOperation gitOperation() {
        return new GitOperationImpl(credential.createGitCredential());
    }

    private Map<String, Object> constructUriVariablesListCommitsCompare(String projectId, String from, String to) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("projectId", projectId);
        uriVariables.put("from", from);
        uriVariables.put("to", to);
        return uriVariables;
    }

    private Map<String, Object> constructUriVariablesListCommits(String projectId, Pageable page) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("projectId", projectId);
        uriVariables.put("page", page.getPageNumber() + 1);
        uriVariables.put("perPage", page.getPageSize());
        return uriVariables;
    }

    private ResponseEntity<GitLabCompareCommitsRes> callApiListCommitsCompare(String uriTemplate, HttpEntity<String> entity, Map<String, Object> uriVariables) {
        return restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                GitLabCompareCommitsRes.class,
                uriVariables
        );
    }

    private ResponseEntity<GitLabListCommitsCommitRes[]> callApiListCommits(String uriTemplate, HttpEntity<String> entity, Map<String, Object> uriVariables) {
        return restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                entity,
                GitLabListCommitsCommitRes[].class,
                uriVariables
        );
    }

    private List<Commit> mappingListCommitsCompareToInternalModel(ResponseEntity<GitLabCompareCommitsRes> response) {
        List<Commit> commits = new ArrayList<>();
        GitLabCompareCommitsRes compareResponses = response.getBody();
        if (compareResponses != null && compareResponses.getCommits() != null) {
            for (GitLabCompareCommitsRes.CompareCommitRes compareResponse : compareResponses.getCommits()) {
                Commit commit = GitLabListCommitsMapper.toInternalModel(compareResponse);
                if (commit != null) {
                    commits.add(commit);
                }
            }
        }
        return commits;
    }

    private List<Commit> mappingListCommitsToInternalModel(ResponseEntity<GitLabListCommitsCommitRes[]> response) {
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
        return commits;
    }
}

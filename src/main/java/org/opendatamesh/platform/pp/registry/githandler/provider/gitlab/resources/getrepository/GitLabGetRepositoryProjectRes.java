package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitLabGetRepositoryProjectRes {
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
    private GitLabGetRepositoryNamespaceRes namespace;
    @JsonProperty("creator_id")
    private Long creatorId;

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

    public GitLabGetRepositoryNamespaceRes getNamespace() {
        return namespace;
    }

    public void setNamespace(GitLabGetRepositoryNamespaceRes namespace) {
        this.namespace = namespace;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}


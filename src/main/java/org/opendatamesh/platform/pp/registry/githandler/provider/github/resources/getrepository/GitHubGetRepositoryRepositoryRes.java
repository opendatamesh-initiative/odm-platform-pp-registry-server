package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubGetRepositoryRepositoryRes {
    private long id;
    private String name;
    private String description;

    @JsonProperty("clone_url")
    private String clone_url;

    @JsonProperty("ssh_url")
    private String ssh_url;

    @JsonProperty("default_branch")
    private String default_branch;

    @JsonProperty("private")
    private boolean isPrivate;
    private GitHubGetRepositoryUserRes owner;

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

    public GitHubGetRepositoryUserRes getOwner() {
        return owner;
    }

    public void setOwner(GitHubGetRepositoryUserRes owner) {
        this.owner = owner;
    }
}


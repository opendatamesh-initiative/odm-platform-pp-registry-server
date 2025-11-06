package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketGetRepositoryRepositoryRes {
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("is_private")
    private boolean isPrivate;
    @JsonProperty("mainbranch")
    private BitbucketGetRepositoryMainBranchRes mainbranch;
    @JsonProperty("owner")
    private BitbucketGetRepositoryUserRes owner;
    @JsonProperty("project")
    private BitbucketGetRepositoryRepositoryProjectRes project;
    @JsonProperty("links")
    private BitbucketGetRepositoryLinksRes links;

    public BitbucketGetRepositoryRepositoryRes() {
    }

    public BitbucketGetRepositoryRepositoryRes(String uuid, String name, String description, boolean isPrivate,
                                       BitbucketGetRepositoryMainBranchRes mainbranch, BitbucketGetRepositoryUserRes owner,
                                       BitbucketGetRepositoryRepositoryProjectRes project, BitbucketGetRepositoryLinksRes links) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
        this.mainbranch = mainbranch;
        this.owner = owner;
        this.project = project;
        this.links = links;
    }

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

    public BitbucketGetRepositoryMainBranchRes getMainbranch() {
        return mainbranch;
    }

    public void setMainbranch(BitbucketGetRepositoryMainBranchRes mainbranch) {
        this.mainbranch = mainbranch;
    }

    public BitbucketGetRepositoryUserRes getOwner() {
        return owner;
    }

    public void setOwner(BitbucketGetRepositoryUserRes owner) {
        this.owner = owner;
    }

    public BitbucketGetRepositoryRepositoryProjectRes getProject() {
        return project;
    }

    public void setProject(BitbucketGetRepositoryRepositoryProjectRes project) {
        this.project = project;
    }

    public BitbucketGetRepositoryLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketGetRepositoryLinksRes links) {
        this.links = links;
    }
}


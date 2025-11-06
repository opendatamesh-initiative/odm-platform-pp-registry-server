package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketCreateRepositoryRepositoryRes {
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("is_private")
    private boolean isPrivate;
    @JsonProperty("mainbranch")
    private BitbucketCreateRepositoryMainBranchRes mainbranch;
    @JsonProperty("owner")
    private BitbucketCreateRepositoryUserRes owner;
    @JsonProperty("project")
    private BitbucketCreateRepositoryRepositoryProjectRes project;
    @JsonProperty("links")
    private BitbucketCreateRepositoryLinksRes links;

    public BitbucketCreateRepositoryRepositoryRes() {
    }

    public BitbucketCreateRepositoryRepositoryRes(String uuid, String name, String description, boolean isPrivate,
                                       BitbucketCreateRepositoryMainBranchRes mainbranch, BitbucketCreateRepositoryUserRes owner,
                                       BitbucketCreateRepositoryRepositoryProjectRes project, BitbucketCreateRepositoryLinksRes links) {
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

    public BitbucketCreateRepositoryMainBranchRes getMainbranch() {
        return mainbranch;
    }

    public void setMainbranch(BitbucketCreateRepositoryMainBranchRes mainbranch) {
        this.mainbranch = mainbranch;
    }

    public BitbucketCreateRepositoryUserRes getOwner() {
        return owner;
    }

    public void setOwner(BitbucketCreateRepositoryUserRes owner) {
        this.owner = owner;
    }

    public BitbucketCreateRepositoryRepositoryProjectRes getProject() {
        return project;
    }

    public void setProject(BitbucketCreateRepositoryRepositoryProjectRes project) {
        this.project = project;
    }

    public BitbucketCreateRepositoryLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketCreateRepositoryLinksRes links) {
        this.links = links;
    }
}


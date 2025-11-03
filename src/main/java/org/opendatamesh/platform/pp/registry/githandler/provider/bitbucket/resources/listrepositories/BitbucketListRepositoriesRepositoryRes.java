package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListRepositoriesRepositoryRes {
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("is_private")
    private boolean isPrivate;
    @JsonProperty("mainbranch")
    private BitbucketListRepositoriesMainBranchRes mainbranch;
    @JsonProperty("owner")
    private BitbucketListRepositoriesUserRes owner;
    @JsonProperty("project")
    private BitbucketListRepositoriesRepositoryProjectRes project;
    @JsonProperty("links")
    private BitbucketListRepositoriesLinksRes links;

    public BitbucketListRepositoriesRepositoryRes() {
    }

    public BitbucketListRepositoriesRepositoryRes(String uuid, String name, String description, boolean isPrivate,
                                       BitbucketListRepositoriesMainBranchRes mainbranch, BitbucketListRepositoriesUserRes owner,
                                       BitbucketListRepositoriesRepositoryProjectRes project, BitbucketListRepositoriesLinksRes links) {
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

    public BitbucketListRepositoriesMainBranchRes getMainbranch() {
        return mainbranch;
    }

    public void setMainbranch(BitbucketListRepositoriesMainBranchRes mainbranch) {
        this.mainbranch = mainbranch;
    }

    public BitbucketListRepositoriesUserRes getOwner() {
        return owner;
    }

    public void setOwner(BitbucketListRepositoriesUserRes owner) {
        this.owner = owner;
    }

    public BitbucketListRepositoriesRepositoryProjectRes getProject() {
        return project;
    }

    public void setProject(BitbucketListRepositoriesRepositoryProjectRes project) {
        this.project = project;
    }

    public BitbucketListRepositoriesLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketListRepositoriesLinksRes links) {
        this.links = links;
    }
}


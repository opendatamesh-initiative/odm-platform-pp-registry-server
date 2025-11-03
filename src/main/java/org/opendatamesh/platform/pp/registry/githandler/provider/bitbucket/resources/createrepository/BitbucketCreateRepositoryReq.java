package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketCreateRepositoryReq {
    @JsonProperty("scm")
    private String scm;

    @JsonProperty("is_private")
    private boolean isPrivate;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("project")
    private BitbucketCreateRepositoryProjectReq project;

    public BitbucketCreateRepositoryReq() {
    }

    public BitbucketCreateRepositoryReq(String scm, boolean isPrivate, String name, String description) {
        this.scm = scm;
        this.isPrivate = isPrivate;
        this.name = name;
        this.description = description;
    }

    public BitbucketCreateRepositoryReq(String scm, boolean isPrivate, String name, String description, BitbucketCreateRepositoryProjectReq project) {
        this.scm = scm;
        this.isPrivate = isPrivate;
        this.name = name;
        this.description = description;
        this.project = project;
    }


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

    public BitbucketCreateRepositoryProjectReq getProject() {
        return project;
    }

    public void setProject(BitbucketCreateRepositoryProjectReq project) {
        this.project = project;
    }
}


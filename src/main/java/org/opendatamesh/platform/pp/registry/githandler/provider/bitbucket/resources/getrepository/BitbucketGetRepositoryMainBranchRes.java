package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketGetRepositoryMainBranchRes {
    @JsonProperty("name")
    private String name;

    public BitbucketGetRepositoryMainBranchRes() {
    }

    public BitbucketGetRepositoryMainBranchRes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


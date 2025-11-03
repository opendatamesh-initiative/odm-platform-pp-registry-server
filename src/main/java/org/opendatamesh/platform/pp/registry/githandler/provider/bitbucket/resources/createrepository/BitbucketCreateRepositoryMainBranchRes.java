package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketCreateRepositoryMainBranchRes {
    @JsonProperty("name")
    private String name;

    public BitbucketCreateRepositoryMainBranchRes() {
    }

    public BitbucketCreateRepositoryMainBranchRes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


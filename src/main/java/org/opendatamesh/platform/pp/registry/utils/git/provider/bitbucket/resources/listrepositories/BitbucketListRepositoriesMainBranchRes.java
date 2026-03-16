package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.resources.listrepositories;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListRepositoriesMainBranchRes {
    @JsonProperty("name")
    private String name;

    public BitbucketListRepositoriesMainBranchRes() {
    }

    public BitbucketListRepositoriesMainBranchRes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


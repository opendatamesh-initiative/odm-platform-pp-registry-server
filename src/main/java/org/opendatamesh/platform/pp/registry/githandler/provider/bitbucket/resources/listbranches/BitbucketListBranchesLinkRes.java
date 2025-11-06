package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListBranchesLinkRes {
    @JsonProperty("name")
    private String name;
    @JsonProperty("href")
    private String href;

    public BitbucketListBranchesLinkRes() {
    }

    public BitbucketListBranchesLinkRes(String name, String href) {
        this.name = name;
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}


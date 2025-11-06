package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListProjectsLinkRes {
    @JsonProperty("href")
    private String href;

    public BitbucketListProjectsLinkRes() {
    }

    public BitbucketListProjectsLinkRes(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}


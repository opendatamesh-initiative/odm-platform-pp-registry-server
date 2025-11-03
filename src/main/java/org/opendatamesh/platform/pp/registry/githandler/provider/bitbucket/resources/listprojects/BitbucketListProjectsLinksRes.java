package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListProjectsLinksRes {
    @JsonProperty("html")
    private BitbucketListProjectsLinkRes html;

    public BitbucketListProjectsLinksRes() {
    }

    public BitbucketListProjectsLinksRes(BitbucketListProjectsLinkRes html) {
        this.html = html;
    }

    public BitbucketListProjectsLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketListProjectsLinkRes html) {
        this.html = html;
    }
}


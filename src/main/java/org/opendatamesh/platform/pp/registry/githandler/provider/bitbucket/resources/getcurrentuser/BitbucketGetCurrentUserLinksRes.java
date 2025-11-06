package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getcurrentuser;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketGetCurrentUserLinksRes {
    @JsonProperty("avatar")
    private BitbucketGetCurrentUserLinkRes avatar;
    @JsonProperty("html")
    private BitbucketGetCurrentUserLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketGetCurrentUserLinkRes> clone;

    public BitbucketGetCurrentUserLinksRes() {
    }

    public BitbucketGetCurrentUserLinksRes(BitbucketGetCurrentUserLinkRes avatar, BitbucketGetCurrentUserLinkRes html, List<BitbucketGetCurrentUserLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketGetCurrentUserLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketGetCurrentUserLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketGetCurrentUserLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketGetCurrentUserLinkRes html) {
        this.html = html;
    }

    public List<BitbucketGetCurrentUserLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketGetCurrentUserLinkRes> clone) {
        this.clone = clone;
    }
}


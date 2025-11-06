package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketListCommitsLinksRes {
    @JsonProperty("avatar")
    private BitbucketListCommitsLinkRes avatar;
    @JsonProperty("html")
    private BitbucketListCommitsLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketListCommitsLinkRes> clone;

    public BitbucketListCommitsLinksRes() {
    }

    public BitbucketListCommitsLinksRes(BitbucketListCommitsLinkRes avatar, BitbucketListCommitsLinkRes html, List<BitbucketListCommitsLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketListCommitsLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketListCommitsLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketListCommitsLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketListCommitsLinkRes html) {
        this.html = html;
    }

    public List<BitbucketListCommitsLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketListCommitsLinkRes> clone) {
        this.clone = clone;
    }
}


package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.checkconnection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketCheckConnectionLinksRes {
    @JsonProperty("avatar")
    private BitbucketCheckConnectionLinkRes avatar;
    @JsonProperty("html")
    private BitbucketCheckConnectionLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketCheckConnectionLinkRes> clone;

    public BitbucketCheckConnectionLinksRes() {
    }

    public BitbucketCheckConnectionLinksRes(BitbucketCheckConnectionLinkRes avatar, BitbucketCheckConnectionLinkRes html, List<BitbucketCheckConnectionLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketCheckConnectionLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketCheckConnectionLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketCheckConnectionLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketCheckConnectionLinkRes html) {
        this.html = html;
    }

    public List<BitbucketCheckConnectionLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketCheckConnectionLinkRes> clone) {
        this.clone = clone;
    }
}


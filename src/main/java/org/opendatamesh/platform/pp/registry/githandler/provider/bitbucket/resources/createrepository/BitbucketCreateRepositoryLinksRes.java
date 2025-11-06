package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketCreateRepositoryLinksRes {
    @JsonProperty("avatar")
    private BitbucketCreateRepositoryLinkRes avatar;
    @JsonProperty("html")
    private BitbucketCreateRepositoryLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketCreateRepositoryLinkRes> clone;

    public BitbucketCreateRepositoryLinksRes() {
    }

    public BitbucketCreateRepositoryLinksRes(BitbucketCreateRepositoryLinkRes avatar, BitbucketCreateRepositoryLinkRes html, List<BitbucketCreateRepositoryLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketCreateRepositoryLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketCreateRepositoryLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketCreateRepositoryLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketCreateRepositoryLinkRes html) {
        this.html = html;
    }

    public List<BitbucketCreateRepositoryLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketCreateRepositoryLinkRes> clone) {
        this.clone = clone;
    }
}


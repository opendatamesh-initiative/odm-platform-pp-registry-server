package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketGetRepositoryLinksRes {
    @JsonProperty("avatar")
    private BitbucketGetRepositoryLinkRes avatar;
    @JsonProperty("html")
    private BitbucketGetRepositoryLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketGetRepositoryLinkRes> clone;

    public BitbucketGetRepositoryLinksRes() {
    }

    public BitbucketGetRepositoryLinksRes(BitbucketGetRepositoryLinkRes avatar, BitbucketGetRepositoryLinkRes html, List<BitbucketGetRepositoryLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketGetRepositoryLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketGetRepositoryLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketGetRepositoryLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketGetRepositoryLinkRes html) {
        this.html = html;
    }

    public List<BitbucketGetRepositoryLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketGetRepositoryLinkRes> clone) {
        this.clone = clone;
    }
}


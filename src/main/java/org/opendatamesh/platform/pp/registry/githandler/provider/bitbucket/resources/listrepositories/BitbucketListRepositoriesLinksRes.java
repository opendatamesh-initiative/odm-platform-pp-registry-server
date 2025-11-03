package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketListRepositoriesLinksRes {
    @JsonProperty("avatar")
    private BitbucketListRepositoriesLinkRes avatar;
    @JsonProperty("html")
    private BitbucketListRepositoriesLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketListRepositoriesLinkRes> clone;

    public BitbucketListRepositoriesLinksRes() {
    }

    public BitbucketListRepositoriesLinksRes(BitbucketListRepositoriesLinkRes avatar, BitbucketListRepositoriesLinkRes html, List<BitbucketListRepositoriesLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketListRepositoriesLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketListRepositoriesLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketListRepositoriesLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketListRepositoriesLinkRes html) {
        this.html = html;
    }

    public List<BitbucketListRepositoriesLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketListRepositoriesLinkRes> clone) {
        this.clone = clone;
    }
}


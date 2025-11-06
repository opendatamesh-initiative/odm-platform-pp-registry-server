package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketListMembersLinksRes {
    @JsonProperty("avatar")
    private BitbucketListMembersLinkRes avatar;
    @JsonProperty("html")
    private BitbucketListMembersLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketListMembersLinkRes> clone;

    public BitbucketListMembersLinksRes() {
    }

    public BitbucketListMembersLinksRes(BitbucketListMembersLinkRes avatar, BitbucketListMembersLinkRes html, List<BitbucketListMembersLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketListMembersLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketListMembersLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketListMembersLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketListMembersLinkRes html) {
        this.html = html;
    }

    public List<BitbucketListMembersLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketListMembersLinkRes> clone) {
        this.clone = clone;
    }
}


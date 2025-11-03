package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketListOrganizationsLinksRes {
    @JsonProperty("avatar")
    private BitbucketListOrganizationsLinkRes avatar;
    @JsonProperty("html")
    private BitbucketListOrganizationsLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketListOrganizationsLinkRes> clone;

    public BitbucketListOrganizationsLinksRes() {
    }

    public BitbucketListOrganizationsLinksRes(BitbucketListOrganizationsLinkRes avatar, BitbucketListOrganizationsLinkRes html, List<BitbucketListOrganizationsLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketListOrganizationsLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketListOrganizationsLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketListOrganizationsLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketListOrganizationsLinkRes html) {
        this.html = html;
    }

    public List<BitbucketListOrganizationsLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketListOrganizationsLinkRes> clone) {
        this.clone = clone;
    }
}


package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getorganization;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketGetOrganizationLinksRes {
    @JsonProperty("avatar")
    private BitbucketGetOrganizationLinkRes avatar;
    @JsonProperty("html")
    private BitbucketGetOrganizationLinkRes html;
    @JsonProperty("clone")
    private List<BitbucketGetOrganizationLinkRes> clone;

    public BitbucketGetOrganizationLinksRes() {
    }

    public BitbucketGetOrganizationLinksRes(BitbucketGetOrganizationLinkRes avatar, BitbucketGetOrganizationLinkRes html, List<BitbucketGetOrganizationLinkRes> clone) {
        this.avatar = avatar;
        this.html = html;
        this.clone = clone;
    }

    public BitbucketGetOrganizationLinkRes getAvatar() {
        return avatar;
    }

    public void setAvatar(BitbucketGetOrganizationLinkRes avatar) {
        this.avatar = avatar;
    }

    public BitbucketGetOrganizationLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketGetOrganizationLinkRes html) {
        this.html = html;
    }

    public List<BitbucketGetOrganizationLinkRes> getClone() {
        return clone;
    }

    public void setClone(List<BitbucketGetOrganizationLinkRes> clone) {
        this.clone = clone;
    }
}


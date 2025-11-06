package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListMembersWorkspaceMembershipRes {
    @JsonProperty("user")
    private BitbucketListMembersUserRes user;

    public BitbucketListMembersWorkspaceMembershipRes() {
    }

    public BitbucketListMembersWorkspaceMembershipRes(BitbucketListMembersUserRes user) {
        this.user = user;
    }

    public BitbucketListMembersUserRes getUser() {
        return user;
    }

    public void setUser(BitbucketListMembersUserRes user) {
        this.user = user;
    }
}


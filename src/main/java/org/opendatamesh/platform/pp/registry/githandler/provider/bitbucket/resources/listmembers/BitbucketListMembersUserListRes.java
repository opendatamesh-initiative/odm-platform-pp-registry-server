package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketListMembersUserListRes {
    @JsonProperty("values")
    private List<BitbucketListMembersWorkspaceMembershipRes> values;

    public BitbucketListMembersUserListRes() {
    }

    public BitbucketListMembersUserListRes(List<BitbucketListMembersWorkspaceMembershipRes> values) {
        this.values = values;
    }

    public List<BitbucketListMembersWorkspaceMembershipRes> getValues() {
        return values;
    }

    public void setValues(List<BitbucketListMembersWorkspaceMembershipRes> values) {
        this.values = values;
    }
}


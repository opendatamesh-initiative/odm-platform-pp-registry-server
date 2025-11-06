package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations;

import java.util.List;

public class BitbucketListOrganizationsWorkspaceListRes {
    private List<BitbucketListOrganizationsWorkspaceRes> values;

    public BitbucketListOrganizationsWorkspaceListRes() {
    }

    public BitbucketListOrganizationsWorkspaceListRes(List<BitbucketListOrganizationsWorkspaceRes> values) {
        this.values = values;
    }

    public List<BitbucketListOrganizationsWorkspaceRes> getValues() {
        return values;
    }

    public void setValues(List<BitbucketListOrganizationsWorkspaceRes> values) {
        this.values = values;
    }
}


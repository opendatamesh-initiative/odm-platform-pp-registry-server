package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches;

import java.util.List;

public class BitbucketListBranchesBranchListRes {
    private List<BitbucketListBranchesBranchRes> values;

    public BitbucketListBranchesBranchListRes() {
    }

    public BitbucketListBranchesBranchListRes(List<BitbucketListBranchesBranchRes> values) {
        this.values = values;
    }

    public List<BitbucketListBranchesBranchRes> getValues() {
        return values;
    }

    public void setValues(List<BitbucketListBranchesBranchRes> values) {
        this.values = values;
    }
}


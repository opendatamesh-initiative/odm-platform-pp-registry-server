package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches;

public class BitbucketListBranchesBranchTargetRes {
    private String hash;

    public BitbucketListBranchesBranchTargetRes() {
    }

    public BitbucketListBranchesBranchTargetRes(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}


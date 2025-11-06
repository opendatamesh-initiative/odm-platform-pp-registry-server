package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listbranches;

public class BitbucketListBranchesBranchRes {
    private String name;
    private BitbucketListBranchesBranchTargetRes target;
    private BitbucketListBranchesBranchLinksRes links;

    public BitbucketListBranchesBranchRes() {
    }

    public BitbucketListBranchesBranchRes(String name, BitbucketListBranchesBranchTargetRes target, BitbucketListBranchesBranchLinksRes links) {
        this.name = name;
        this.target = target;
        this.links = links;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BitbucketListBranchesBranchTargetRes getTarget() {
        return target;
    }

    public void setTarget(BitbucketListBranchesBranchTargetRes target) {
        this.target = target;
    }

    public BitbucketListBranchesBranchLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketListBranchesBranchLinksRes links) {
        this.links = links;
    }
}


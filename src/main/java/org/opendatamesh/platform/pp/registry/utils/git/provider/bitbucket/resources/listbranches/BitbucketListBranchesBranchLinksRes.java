package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.resources.listbranches;

public class BitbucketListBranchesBranchLinksRes {
    private BitbucketListBranchesLinkRes html;

    public BitbucketListBranchesBranchLinksRes() {
    }

    public BitbucketListBranchesBranchLinksRes(BitbucketListBranchesLinkRes html) {
        this.html = html;
    }

    public BitbucketListBranchesLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketListBranchesLinkRes html) {
        this.html = html;
    }
}


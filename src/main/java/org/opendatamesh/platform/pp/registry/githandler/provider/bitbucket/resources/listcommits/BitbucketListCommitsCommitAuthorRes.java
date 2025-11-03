package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits;

public class BitbucketListCommitsCommitAuthorRes {
    private BitbucketListCommitsUserRes user;

    public BitbucketListCommitsCommitAuthorRes() {
    }

    public BitbucketListCommitsCommitAuthorRes(BitbucketListCommitsUserRes user) {
        this.user = user;
    }

    public BitbucketListCommitsUserRes getUser() {
        return user;
    }

    public void setUser(BitbucketListCommitsUserRes user) {
        this.user = user;
    }
}


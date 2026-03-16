package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.resources.listcommits;

public class BitbucketListCommitsCommitAuthorRes {
    private String raw;
    private BitbucketListCommitsUserRes user;

    public BitbucketListCommitsCommitAuthorRes() {
    }

    public BitbucketListCommitsCommitAuthorRes(String raw, BitbucketListCommitsUserRes user) {
        this.raw = raw;
        this.user = user;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public BitbucketListCommitsUserRes getUser() {
        return user;
    }

    public void setUser(BitbucketListCommitsUserRes user) {
        this.user = user;
    }
}


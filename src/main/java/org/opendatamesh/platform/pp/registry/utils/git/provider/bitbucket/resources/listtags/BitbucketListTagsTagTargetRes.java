package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.resources.listtags;

public class BitbucketListTagsTagTargetRes {
    private String hash;

    public BitbucketListTagsTagTargetRes() {
    }

    public BitbucketListTagsTagTargetRes(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}


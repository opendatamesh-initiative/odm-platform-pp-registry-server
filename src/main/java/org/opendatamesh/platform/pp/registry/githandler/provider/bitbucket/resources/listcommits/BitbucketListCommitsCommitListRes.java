package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits;

import java.util.List;

public class BitbucketListCommitsCommitListRes {
    private List<BitbucketListCommitsCommitRes> values;

    public BitbucketListCommitsCommitListRes() {
    }

    public BitbucketListCommitsCommitListRes(List<BitbucketListCommitsCommitRes> values) {
        this.values = values;
    }

    public List<BitbucketListCommitsCommitRes> getValues() {
        return values;
    }

    public void setValues(List<BitbucketListCommitsCommitRes> values) {
        this.values = values;
    }
}


package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags;

import java.util.List;

public class BitbucketListTagsTagListRes {
    private List<BitbucketListTagsTagRes> values;

    public BitbucketListTagsTagListRes() {
    }

    public BitbucketListTagsTagListRes(List<BitbucketListTagsTagRes> values) {
        this.values = values;
    }

    public List<BitbucketListTagsTagRes> getValues() {
        return values;
    }

    public void setValues(List<BitbucketListTagsTagRes> values) {
        this.values = values;
    }
}


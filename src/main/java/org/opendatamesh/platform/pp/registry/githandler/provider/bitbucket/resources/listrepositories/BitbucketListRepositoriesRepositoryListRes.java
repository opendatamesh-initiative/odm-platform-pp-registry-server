package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories;

import java.util.List;

public class BitbucketListRepositoriesRepositoryListRes {
    private List<BitbucketListRepositoriesRepositoryRes> values;

    public BitbucketListRepositoriesRepositoryListRes() {
    }

    public BitbucketListRepositoriesRepositoryListRes(List<BitbucketListRepositoriesRepositoryRes> values) {
        this.values = values;
    }

    public List<BitbucketListRepositoriesRepositoryRes> getValues() {
        return values;
    }

    public void setValues(List<BitbucketListRepositoriesRepositoryRes> values) {
        this.values = values;
    }
}


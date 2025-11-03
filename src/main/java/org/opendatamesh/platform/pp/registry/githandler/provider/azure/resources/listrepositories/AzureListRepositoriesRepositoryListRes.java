package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories;

import java.util.List;

public class AzureListRepositoriesRepositoryListRes {
    private List<AzureListRepositoriesRepositoryRes> value;

    public List<AzureListRepositoriesRepositoryRes> getValue() {
        return value;
    }

    public void setValue(List<AzureListRepositoriesRepositoryRes> value) {
        this.value = value;
    }
}


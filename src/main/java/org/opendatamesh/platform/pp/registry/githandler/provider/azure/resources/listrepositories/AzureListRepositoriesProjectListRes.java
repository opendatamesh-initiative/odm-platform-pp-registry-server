package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories;

import java.util.List;

public class AzureListRepositoriesProjectListRes {
    private List<AzureListRepositoriesProjectRes> value;

    public List<AzureListRepositoriesProjectRes> getValue() {
        return value;
    }

    public void setValue(List<AzureListRepositoriesProjectRes> value) {
        this.value = value;
    }
}


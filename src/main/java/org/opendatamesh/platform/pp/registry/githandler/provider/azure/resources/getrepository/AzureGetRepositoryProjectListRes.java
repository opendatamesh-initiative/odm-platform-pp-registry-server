package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getrepository;

import java.util.List;

public class AzureGetRepositoryProjectListRes {
    private List<AzureGetRepositoryProjectRes> value;

    public List<AzureGetRepositoryProjectRes> getValue() {
        return value;
    }

    public void setValue(List<AzureGetRepositoryProjectRes> value) {
        this.value = value;
    }
}


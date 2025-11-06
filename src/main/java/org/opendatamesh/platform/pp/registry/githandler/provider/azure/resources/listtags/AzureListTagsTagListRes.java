package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listtags;

import java.util.List;

public class AzureListTagsTagListRes {
    private List<AzureListTagsTagRes> value;

    public List<AzureListTagsTagRes> getValue() {
        return value;
    }

    public void setValue(List<AzureListTagsTagRes> value) {
        this.value = value;
    }
}


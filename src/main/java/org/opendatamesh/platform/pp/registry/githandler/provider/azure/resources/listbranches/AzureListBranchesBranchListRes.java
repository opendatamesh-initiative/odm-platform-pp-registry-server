package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listbranches;

import java.util.List;

public class AzureListBranchesBranchListRes {
    private List<AzureListBranchesBranchRes> value;

    public List<AzureListBranchesBranchRes> getValue() {
        return value;
    }

    public void setValue(List<AzureListBranchesBranchRes> value) {
        this.value = value;
    }
}


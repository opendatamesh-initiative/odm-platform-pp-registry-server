package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits;

import java.util.List;

public class AzureListCommitsCommitListRes {
    private List<AzureListCommitsCommitRes> value;

    public List<AzureListCommitsCommitRes> getValue() {
        return value;
    }

    public void setValue(List<AzureListCommitsCommitRes> value) {
        this.value = value;
    }
}


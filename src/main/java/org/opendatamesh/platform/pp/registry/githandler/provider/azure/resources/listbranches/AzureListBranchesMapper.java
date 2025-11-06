package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listbranches;

import org.opendatamesh.platform.pp.registry.githandler.model.Branch;

public abstract class AzureListBranchesMapper {

    public static Branch toInternalModel(AzureListBranchesBranchRes branchRes) {
        if (branchRes == null) {
            return null;
        }

        return new Branch(
                branchRes.getName().replace("refs/heads/", ""),
                branchRes.getObjectId()
        );
    }
}


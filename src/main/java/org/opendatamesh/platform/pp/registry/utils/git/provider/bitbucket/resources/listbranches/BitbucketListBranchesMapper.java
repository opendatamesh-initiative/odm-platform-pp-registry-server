package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.resources.listbranches;

import org.opendatamesh.platform.pp.registry.utils.git.model.Branch;

public abstract class BitbucketListBranchesMapper {

    /**
     * Maps BitbucketListBranchesBranchRes to internal Branch model
     */
    public static Branch toInternalModel(BitbucketListBranchesBranchRes branchRes) {
        if (branchRes == null) {
            return null;
        }
        
        String commitHash = null;
        if (branchRes.getTarget() != null) {
            commitHash = branchRes.getTarget().getHash();
        }

        return new Branch(branchRes.getName(), commitHash);
    }
}


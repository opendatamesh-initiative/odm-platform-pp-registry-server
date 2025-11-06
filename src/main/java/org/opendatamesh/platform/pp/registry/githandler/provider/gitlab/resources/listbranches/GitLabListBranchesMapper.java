package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listbranches;

import org.opendatamesh.platform.pp.registry.githandler.model.Branch;

public abstract class GitLabListBranchesMapper {

    public static Branch toInternalModel(GitLabListBranchesBranchRes branchRes) {
        if (branchRes == null) {
            return null;
        }

        Branch branch = new Branch(
                branchRes.getName(),
                branchRes.getCommit() != null ? branchRes.getCommit().getId() : null
        );
        branch.setProtected(branchRes.isProtected());
        branch.setDefault(branchRes.isDefault());

        return branch;
    }
}


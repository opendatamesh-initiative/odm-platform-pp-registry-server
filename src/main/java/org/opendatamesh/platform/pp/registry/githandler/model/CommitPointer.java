package org.opendatamesh.platform.pp.registry.githandler.model;

public record CommitPointer(
        String fromTagName,
        String toTagName,
        String fromCommitHash,
        String toCommitHash,
        String fromBranchName,
        String toBranchName,
        String branchName) {
}

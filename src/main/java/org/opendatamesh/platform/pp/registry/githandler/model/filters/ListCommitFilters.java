package org.opendatamesh.platform.pp.registry.githandler.model.filters;

public record ListCommitFilters (String fromTagName, String toTagName, String fromCommitHash, String toCommitHash, String fromBranchName, String toBranchName) {

    public ListCommitFilters() {
        this(null, null, null, null, null, null);
    }

}

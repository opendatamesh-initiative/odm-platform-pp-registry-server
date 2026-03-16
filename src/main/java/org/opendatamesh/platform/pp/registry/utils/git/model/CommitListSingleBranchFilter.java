package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * Filter: list commits on a single branch.
 */
public record CommitListSingleBranchFilter(CommitRefBranch branch) implements CommitListFilter {
    public CommitListSingleBranchFilter {
        if (branch == null) {
            throw new IllegalArgumentException("branch must not be null");
        }
    }
}

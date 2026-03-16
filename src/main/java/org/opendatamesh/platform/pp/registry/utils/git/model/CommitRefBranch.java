package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * A commit reference by branch name.
 */
public class CommitRefBranch extends CommitRef {
    private final String name;

    public CommitRefBranch(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getRefValue() {
        return name;
    }

    @Override
    public RefType getRefType() {
        return RefType.BRANCH;
    }
}

package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * A repository pointer by branch name.
 */
public class RepositoryPointerBranch extends RepositoryPointer {
    private final String name;

    public RepositoryPointerBranch(String name) {
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

    @Override
    public boolean isReadOnly() {
        return false;
    }
}

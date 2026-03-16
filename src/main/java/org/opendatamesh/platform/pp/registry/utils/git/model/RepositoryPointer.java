package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * Represents a pointer into a repository (branch, tag, or commit) used when cloning/checking out.
 * Does not hold the repository; repository is passed separately where needed.
 */
public abstract class RepositoryPointer {

    public enum RefType {
        TAG,
        BRANCH,
        COMMIT
    }

    /**
     * Returns the string value for clone/checkout (branch name, tag name, or commit hash).
     */
    public abstract String getRefValue();

    /**
     * Returns the type of pointer for clone/checkout logic.
     */
    public abstract RefType getRefType();

    /**
     * Whether this pointer is read-only (tag and commit are read-only, branch is not).
     */
    public abstract boolean isReadOnly();
}

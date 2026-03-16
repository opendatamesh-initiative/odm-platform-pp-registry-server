package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * Represents a single commit reference (tag, branch, or commit hash) used as an
 * endpoint for listing or comparing commits.
 */
public abstract class CommitRef {

    public enum RefType {
        TAG,
        BRANCH,
        COMMIT
    }

    /**
     * Returns the string value to pass to the provider API (tag name, branch name, or commit hash).
     */
    public abstract String getRefValue();

    /**
     * Returns the type of reference for providers that need to distinguish (e.g. Azure DevOps).
     */
    public abstract RefType getRefType();
}

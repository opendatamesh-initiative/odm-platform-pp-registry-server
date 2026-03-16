package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * A commit reference by tag name.
 */
public class CommitRefTag extends CommitRef {
    private final String name;

    public CommitRefTag(String name) {
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
        return RefType.TAG;
    }
}

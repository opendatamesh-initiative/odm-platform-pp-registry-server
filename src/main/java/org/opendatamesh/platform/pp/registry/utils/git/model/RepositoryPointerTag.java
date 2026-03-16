package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * A repository pointer by tag name.
 */
public class RepositoryPointerTag extends RepositoryPointer {
    private final String name;

    public RepositoryPointerTag(String name) {
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

    @Override
    public boolean isReadOnly() {
        return true;
    }
}

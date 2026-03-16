package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * A repository pointer by commit hash (SHA).
 */
public class RepositoryPointerCommit extends RepositoryPointer {
    private final String hash;

    public RepositoryPointerCommit(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String getRefValue() {
        return hash;
    }

    @Override
    public RefType getRefType() {
        return RefType.COMMIT;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}

package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * A commit reference by commit hash (SHA).
 */
public class CommitRefHash extends CommitRef {
    private final String hash;

    public CommitRefHash(String hash) {
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
}

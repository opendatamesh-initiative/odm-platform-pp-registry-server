package org.opendatamesh.platform.pp.registry.githandler.model;

public class RepositoryPointerCommit extends RepositoryPointer {
    private final String hash;

    public RepositoryPointerCommit(Repository repository, String hash) {
        super(repository);
        super.readOnly = true;
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}

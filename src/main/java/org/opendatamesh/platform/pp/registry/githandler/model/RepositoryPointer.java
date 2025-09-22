package org.opendatamesh.platform.pp.registry.githandler.model;

public abstract class RepositoryPointer {
    protected final Repository repository;
    protected boolean readOnly;

    protected RepositoryPointer(Repository repository) {
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}

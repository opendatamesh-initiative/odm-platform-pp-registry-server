package org.opendatamesh.platform.pp.registry.githandler.model;

public class RepositoryPointerBranch extends RepositoryPointer {
    private final String name;

    public RepositoryPointerBranch(Repository repository, String name) {
        super(repository);
        super.readOnly = false;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

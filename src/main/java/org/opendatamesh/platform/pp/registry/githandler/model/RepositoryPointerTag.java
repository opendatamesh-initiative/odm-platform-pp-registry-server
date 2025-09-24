package org.opendatamesh.platform.pp.registry.githandler.model;

public class RepositoryPointerTag extends RepositoryPointer {
    private final String name;

    public RepositoryPointerTag(Repository repository, String name) {
        super(repository);
        super.readOnly = true;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

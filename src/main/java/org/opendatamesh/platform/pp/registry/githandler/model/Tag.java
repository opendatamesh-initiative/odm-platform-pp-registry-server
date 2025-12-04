package org.opendatamesh.platform.pp.registry.githandler.model;

/**
 * Represents a Git tag
 */
public class Tag {
    private String name;
    private String commitHash;

    public Tag() {
    }

    public Tag(String name, String commitHash) {
        this.name = name;
        this.commitHash = commitHash;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

}

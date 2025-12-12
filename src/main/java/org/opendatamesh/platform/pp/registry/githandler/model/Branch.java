package org.opendatamesh.platform.pp.registry.githandler.model;

import java.util.Objects;

/**
 * Represents a Git branch
 */
public class Branch {
    private String name;
    private String commitHash;
    private boolean isDefault;
    private boolean isProtected;

    public Branch() {
    }

    public Branch(String name, String commitHash) {
        this.name = name;
        this.commitHash = commitHash;
    }

    public Branch(String name, String commitHash, boolean isDefault) {
        this.name = name;
        this.commitHash = commitHash;
        this.isDefault = isDefault;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }
}

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
    private String url;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(name, branch.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "name='" + name + '\'' +
                ", commitHash='" + commitHash + '\'' +
                ", isDefault=" + isDefault +
                ", isProtected=" + isProtected +
                ", url='" + url + '\'' +
                '}';
    }
}

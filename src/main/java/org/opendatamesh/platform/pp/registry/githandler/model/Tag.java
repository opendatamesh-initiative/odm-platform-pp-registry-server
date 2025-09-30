package org.opendatamesh.platform.pp.registry.githandler.model;

import java.util.Date;
import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                ", commitHash='" + commitHash + '\'' +
                '}';
    }
}

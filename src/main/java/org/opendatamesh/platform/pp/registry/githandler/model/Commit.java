package org.opendatamesh.platform.pp.registry.githandler.model;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a Git commit
 */
public class Commit {
    private String hash;
    private String message;
    private String authorEmail;
    private Date commitDate;

    public Commit() {
    }

    public Commit(String hash, String message, String authorEmail, Date commitDate) {
        this.hash = hash;
        this.message = message;
        this.authorEmail = authorEmail;
        this.commitDate = commitDate;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit commit = (Commit) o;
        return Objects.equals(hash, commit.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "Commit{" +
                "hash='" + hash + '\'' +
                ", message='" + message + '\'' +
                ", authorEmail='" + authorEmail + '\'' +
                ", commitDate=" + commitDate +
                '}';
    }
}

package org.opendatamesh.platform.pp.registry.githandler.model;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a Git commit
 */
public class Commit {
    private String hash;
    private String message;
    private String author;
    private String authorEmail;
    private Date commitDate;
    private String committer;
    private String committerEmail;
    private Date authorDate;
    private String url;

    public Commit() {
    }

    public Commit(String hash, String message, String author, String authorEmail, Date commitDate) {
        this.hash = hash;
        this.message = message;
        this.author = author;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    public Date getAuthorDate() {
        return authorDate;
    }

    public void setAuthorDate(Date authorDate) {
        this.authorDate = authorDate;
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
                ", author='" + author + '\'' +
                ", authorEmail='" + authorEmail + '\'' +
                ", commitDate=" + commitDate +
                ", committer='" + committer + '\'' +
                ", committerEmail='" + committerEmail + '\'' +
                ", authorDate=" + authorDate +
                ", url='" + url + '\'' +
                '}';
    }
}

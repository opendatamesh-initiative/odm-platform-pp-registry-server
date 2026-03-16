package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * Represents a Git tag
 */
public class Tag {
    private String name;
    private String commitHash;
    private String author;
    private String authorEmail;
    private String message;

    public Tag() {
    }

    public Tag(String name, String commitHash) {
        this.name = name;
        this.commitHash = commitHash;
    }

    public Tag(String name, String commitHash, String author, String authorEmail, String message) {
        this.name = name;
        this.commitHash = commitHash;
        this.author = author;
        this.authorEmail = authorEmail;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

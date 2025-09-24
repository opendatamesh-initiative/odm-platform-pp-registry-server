package org.opendatamesh.platform.pp.registry.githandler.model;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a Git tag
 */
public class Tag {
    private String name;
    private String commitHash;
    private String message;
    private String tagger;
    private String taggerEmail;
    private Date tagDate;
    private String url;

    public Tag() {
    }

    public Tag(String name, String commitHash) {
        this.name = name;
        this.commitHash = commitHash;
    }

    public Tag(String name, String commitHash, String message) {
        this.name = name;
        this.commitHash = commitHash;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTagger() {
        return tagger;
    }

    public void setTagger(String tagger) {
        this.tagger = tagger;
    }

    public String getTaggerEmail() {
        return taggerEmail;
    }

    public void setTaggerEmail(String taggerEmail) {
        this.taggerEmail = taggerEmail;
    }

    public Date getTagDate() {
        return tagDate;
    }

    public void setTagDate(Date tagDate) {
        this.tagDate = tagDate;
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
                ", message='" + message + '\'' +
                ", tagger='" + tagger + '\'' +
                ", taggerEmail='" + taggerEmail + '\'' +
                ", tagDate=" + tagDate +
                ", url='" + url + '\'' +
                '}';
    }
}

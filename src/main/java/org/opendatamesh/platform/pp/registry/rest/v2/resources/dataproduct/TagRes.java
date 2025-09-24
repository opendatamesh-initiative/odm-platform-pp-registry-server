package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Git tag information")
public class TagRes {

    @Schema(description = "Tag name", example = "v1.0.0")
    private String name;

    @Schema(description = "Commit hash", example = "abc123def456")
    private String commitHash;

    @Schema(description = "Tag message", example = "Release version 1.0.0")
    private String message;

    @Schema(description = "Tagger name", example = "John Doe")
    private String taggerName;

    @Schema(description = "Tagger email", example = "john.doe@example.com")
    private String taggerEmail;

    @Schema(description = "Tag date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date tagDate;

    @Schema(description = "Tag URL", example = "https://github.com/owner/repo/releases/tag/v1.0.0")
    private String url;

    public TagRes() {}

    public TagRes(String name, String commitHash, String message, String taggerName, String taggerEmail, Date tagDate, String url) {
        this.name = name;
        this.commitHash = commitHash;
        this.message = message;
        this.taggerName = taggerName;
        this.taggerEmail = taggerEmail;
        this.tagDate = tagDate;
        this.url = url;
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

    public String getTaggerName() {
        return taggerName;
    }

    public void setTaggerName(String taggerName) {
        this.taggerName = taggerName;
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
}

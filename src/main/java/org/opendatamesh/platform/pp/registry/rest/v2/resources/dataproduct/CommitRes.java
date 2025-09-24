package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Git commit information")
public class CommitRes {

    @Schema(description = "Commit hash", example = "abc123def456")
    private String hash;

    @Schema(description = "Commit message", example = "Fix: resolve authentication issue")
    private String message;

    @Schema(description = "Author name", example = "John Doe")
    private String authorName;

    @Schema(description = "Author email", example = "john.doe@example.com")
    private String authorEmail;

    @Schema(description = "Author date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date authorDate;

    @Schema(description = "Committer name", example = "John Doe")
    private String committerName;

    @Schema(description = "Committer email", example = "john.doe@example.com")
    private String committerEmail;

    @Schema(description = "Committer date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date committerDate;

    @Schema(description = "Commit URL", example = "https://github.com/owner/repo/commit/abc123def456")
    private String url;

    public CommitRes() {}

    public CommitRes(String hash, String message, String authorName, String authorEmail, Date authorDate, 
                     String committerName, String committerEmail, Date committerDate, String url) {
        this.hash = hash;
        this.message = message;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.authorDate = authorDate;
        this.committerName = committerName;
        this.committerEmail = committerEmail;
        this.committerDate = committerDate;
        this.url = url;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Date getAuthorDate() {
        return authorDate;
    }

    public void setAuthorDate(Date authorDate) {
        this.authorDate = authorDate;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public void setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
    }

    public Date getCommitterDate() {
        return committerDate;
    }

    public void setCommitterDate(Date committerDate) {
        this.committerDate = committerDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

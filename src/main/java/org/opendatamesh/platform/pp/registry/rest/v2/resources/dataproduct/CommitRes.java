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

    @Schema(description = "Author email", example = "john.doe@example.com")
    private String authorEmail;

    @Schema(description = "Commit date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date commitDate;

    public CommitRes() {}

    public CommitRes(String hash, String message, String authorEmail, Date commitDate) {
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
}

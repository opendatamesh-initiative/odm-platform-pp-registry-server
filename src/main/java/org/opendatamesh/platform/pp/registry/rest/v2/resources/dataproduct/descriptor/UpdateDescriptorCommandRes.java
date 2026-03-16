package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateDescriptorCommandRes", description = "Query parameters for updating a data product descriptor in Git (body is the descriptor JSON)")
public class UpdateDescriptorCommandRes {

    @Parameter(description = "The Git branch where the descriptor should be updated")
    @Schema(description = "The Git branch where the descriptor should be updated", required = true)
    private String branch;

    @Parameter(description = "The commit message for the update")
    @Schema(description = "The commit message for the update", required = true)
    private String commitMessage;

    @Parameter(description = "Base commit SHA to ensure consistency")
    @Schema(description = "Base commit SHA to ensure consistency", required = true)
    private String baseCommit;

    @Parameter(description = "Optional author name (username) for the commit")
    @Schema(description = "Optional author name (username) for the commit")
    private String authorName;

    @Parameter(description = "Optional author email for the commit")
    @Schema(description = "Optional author email for the commit")
    private String authorEmail;

    public UpdateDescriptorCommandRes() {
    }

    public UpdateDescriptorCommandRes(String branch, String commitMessage, String baseCommit,
                                      String authorName, String authorEmail) {
        this.branch = branch;
        this.commitMessage = commitMessage;
        this.baseCommit = baseCommit;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getBaseCommit() {
        return baseCommit;
    }

    public void setBaseCommit(String baseCommit) {
        this.baseCommit = baseCommit;
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
}

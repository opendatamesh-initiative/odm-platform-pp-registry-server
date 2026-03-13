package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Git tag information")
public class TagRes {

    @Schema(description = "Tag name", example = "v1.0.0")
    private String name;

    @Schema(description = "Commit hash", example = "abc123def456")
    private String commitHash;

    @Schema(description = "Optional message for an annotated tag. If not provided, a lightweight tag will be created.", example = "Release version 1.0.0")
    private String message;

    @Schema(description = "Optional SHA of the commit to tag. If provided, the tag will point to this commit directly.", example = "a1b2c3d4e5f6g7h8i9j0")
    private String target;

    @Schema(description = "Optional branch name. Used to tag the latest commit (HEAD) on the specified branch if no target SHA is provided.", example = "develop")
    private String branchName;

    @Schema(description = "Optional author name (username) for the tag. When provided, used as the tagger identity in the annotated tag.", example = "jane.doe")
    private String authorName;

    @Schema(description = "Optional author email for the tag. When provided, used as the tagger identity in the annotated tag.", example = "jane.doe@example.com")
    private String authorEmail;

    public TagRes() {
    }

    public TagRes(String name, String commitHash) {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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

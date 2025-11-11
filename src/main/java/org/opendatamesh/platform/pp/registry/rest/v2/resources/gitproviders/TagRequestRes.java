package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a Git tag creation request.")
public class TagRequestRes {

    @Schema(
            description = "The name of the tag to create.",
            example = "v1.0.0",
            required = true
    )
    private String tagName;

    @Schema(
            description = "Optional message for an annotated tag. If not provided, a lightweight tag will be created.",
            example = "Release version 1.0.0"
    )
    private String message;

    @Schema(
            description = "Optional SHA of the commit to tag. If provided, the tag will point to this commit directly.",
            example = "a1b2c3d4e5f6g7h8i9j0"
    )
    private String target;

    @Schema(
            description = "Optional branch name. Used to tag the latest commit (HEAD) on the specified branch if no target SHA is provided.",
            example = "develop"
    )
    private String branchName;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
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
}

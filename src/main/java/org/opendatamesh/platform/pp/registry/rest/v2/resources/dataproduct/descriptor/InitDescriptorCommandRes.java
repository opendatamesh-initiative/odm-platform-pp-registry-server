package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InitDescriptorCommandRes", description = "Query parameters for initializing a data product descriptor in Git (body is the descriptor JSON)")
public class InitDescriptorCommandRes {

    @Parameter(description = "Optional branch where the descriptor should be initialized. Defaults to the repository default branch if not specified.")
    @Schema(description = "Optional branch where the descriptor should be initialized. Defaults to the repository default branch if not specified.")
    private String branch;

    @Parameter(description = "Optional author name (username) for the initial commit")
    @Schema(description = "Optional author name (username) for the initial commit")
    private String authorName;

    @Parameter(description = "Optional author email for the initial commit")
    @Schema(description = "Optional author email for the initial commit")
    private String authorEmail;

    public InitDescriptorCommandRes() {
    }

    public InitDescriptorCommandRes(String branch, String authorName, String authorEmail) {
        this.branch = branch;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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

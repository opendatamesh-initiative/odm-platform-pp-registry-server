package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GetDescriptorOptionsRes", description = "Query parameters for fetching a data product descriptor from Git")
public class GetDescriptorOptionsRes {

    @Parameter(description = "Optional tag to select a specific version")
    @Schema(description = "Optional tag to select a specific version")
    private String tag;

    @Parameter(description = "Optional branch name")
    @Schema(description = "Optional branch name")
    private String branch;

    @Parameter(description = "Optional commit SHA")
    @Schema(description = "Optional commit SHA")
    private String commit;

    public GetDescriptorOptionsRes() {
    }

    /** Git reference built from tag, branch, commit (query params). */
    public GitReference getGitReference() {
        return new GitReference(tag, branch, commit);
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public record GitReference(String tag, String branch, String commit) {
    }
}

package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Git branch information")
public class BranchRes {

    @Schema(description = "Branch name", example = "main")
    private String name;

    @Schema(description = "Latest commit hash", example = "abc123def456")
    private String commitHash;

    @Schema(description = "Whether this is the default branch", example = "true")
    private boolean isDefault;

    @Schema(description = "Whether this branch is protected", example = "false")
    private boolean isProtected;

    @Schema(description = "Branch URL", example = "https://github.com/owner/repo/tree/main")
    private String url;

    public BranchRes() {}

    public BranchRes(String name, String commitHash, boolean isDefault, boolean isProtected, String url) {
        this.name = name;
        this.commitHash = commitHash;
        this.isDefault = isDefault;
        this.isProtected = isProtected;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

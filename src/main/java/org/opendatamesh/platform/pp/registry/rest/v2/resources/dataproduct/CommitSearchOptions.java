package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public class CommitSearchOptions {

    @Parameter(
            description = "Filter commits from a specific tag name (inclusive). Used with 'toTagName' to get commits between two tags.",
            schema = @Schema(type = "string", example = "v1.0.0")
    )
    private String fromTagName;

    @Parameter(
            description = "Filter commits to a specific tag name (inclusive). Used with 'fromTagName' to get commits between two tags.",
            schema = @Schema(type = "string", example = "v2.0.0")
    )
    private String toTagName;

    @Parameter(
            description = "Filter commits to a specific commit hash (inclusive). Used with 'toCommitHash' to get commits between two commit hashes.",
            schema = @Schema(type = "string", example = "c46f4c1")
    )
    private String fromCommitHash;

    @Parameter(
            description = "Filter commits to a specific commit hash (inclusive). Used with 'fromCommitHash' to get commits between two commit hashes.",
            schema = @Schema(type = "string", example = "480ad12")
    )
    private String toCommitHash;

    @Parameter(
            description = "Filter commits to a specific branch name (inclusive). Used with 'toBranchName' to get commits between two branches.",
            schema = @Schema(type = "string", example = "main")
    )
    private String fromBranchName;

    @Parameter(
            description = "Filter commits to a specific branch name (inclusive). Used with 'fromBranchName' to get commits between two branches.",
            schema = @Schema(type = "string", example = "test")
    )
    private String toBranchName;

    public String getFromTagName() {
        return fromTagName;
    }

    public void setFromTagName(String fromTagName) {
        this.fromTagName = fromTagName;
    }

    public String getToTagName() {
        return toTagName;
    }

    public void setToTagName(String toTagName) {
        this.toTagName = toTagName;
    }

    public String getFromCommitHash() {return fromCommitHash;}

    public void setFromCommitHash(String fromCommitHash) { this.fromCommitHash = fromCommitHash;}

    public String getToCommitHash() {
        return toCommitHash;
    }

    public String getFromBranchName() {
        return fromBranchName;
    }

    public String getToBranchName() {
        return toBranchName;
    }

    public void setToCommitHash(String toCommitHash) {
        this.toCommitHash = toCommitHash;
    }

    public void setFromBranchName(String fromBranchName) {
        this.fromBranchName = fromBranchName;
    }

    public void setToBranchName(String toBranchName) {
        this.toBranchName = toBranchName;
    }
}


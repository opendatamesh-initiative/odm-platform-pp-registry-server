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


    public TagRes() {}

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

}

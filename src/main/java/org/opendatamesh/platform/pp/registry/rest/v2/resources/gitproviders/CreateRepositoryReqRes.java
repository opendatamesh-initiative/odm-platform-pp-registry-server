package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateRepositoryReqRes", description = "Request to create a new repository in a Git provider")
public class CreateRepositoryReqRes {

    @Schema(description = "The name of the repository", example = "my-new-repository", required = true)
    private String name;

    @Schema(description = "The description of the repository", example = "A sample repository for demonstration")
    private String description;

    @Schema(description = "Whether the repository should be private", example = "false", required = true)
    private Boolean isPrivate;

    public CreateRepositoryReqRes() {
    }

    public CreateRepositoryReqRes(String name, String description, Boolean isPrivate) {
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}

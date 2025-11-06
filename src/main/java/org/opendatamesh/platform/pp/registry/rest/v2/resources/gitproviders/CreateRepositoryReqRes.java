package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "CreateRepositoryReqRes", description = "Request to create a new repository in a Git provider")
public class CreateRepositoryReqRes {

    @Schema(description = "The name of the repository", example = "my-new-repository")
    private String name;

    @Schema(description = "The description of the repository", example = "A sample repository for demonstration")
    private String description;

    @Schema(description = "Whether the repository should be private", example = "false")
    private Boolean isPrivate;

    @Schema(description = "Additional provider-specific properties", example = "[{\"name\": \"project\", \"value\": {\"key\": \"PROJ\"}}]")
    private List<ProviderCustomResourcePropertyRes> providerCustomResourceProperties;

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

    public List<ProviderCustomResourcePropertyRes> getProviderCustomResourceProperties() {
        return providerCustomResourceProperties;
    }

    public void setProviderCustomResourceProperties(List<ProviderCustomResourcePropertyRes> providerCustomResourceProperties) {
        this.providerCustomResourceProperties = providerCustomResourceProperties;
    }
}

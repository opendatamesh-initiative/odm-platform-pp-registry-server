package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "providerCustomResourceDefinition", description = "Definition of an custom resource")
public class ProviderCustomResourceDefinitionRes {

    @Schema(description = "The name of the resource", example = "workspace")
    private String name;

    @Schema(description = "The type of the resource", example = "object")
    private String type;

    @Schema(description = "Whether this resource is required", example = "true")
    private Boolean required;

    public ProviderCustomResourceDefinitionRes() {
    }

    public ProviderCustomResourceDefinitionRes(String name, String type, Boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}


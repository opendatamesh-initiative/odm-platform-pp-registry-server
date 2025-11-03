package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "additionalProviderProperty", description = "Additional provider-specific property for repositories")
public class ProviderAdditionalPropertyRes {
    
    @Schema(description = "The name of the property", example = "full_name")
    private String name;
    
    @Schema(description = "The value of the property (can be any JSON value)", example = "owner/repo")
    private JsonNode value;

    public ProviderAdditionalPropertyRes() {
    }

    public ProviderAdditionalPropertyRes(String name, JsonNode value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getValue() {
        return value;
    }

    public void setValue(JsonNode value) {
        this.value = value;
    }
}

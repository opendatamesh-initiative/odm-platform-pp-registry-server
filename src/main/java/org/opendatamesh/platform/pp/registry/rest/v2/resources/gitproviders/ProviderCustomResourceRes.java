package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "providerSpecificResource", description = "Provider-specific resource")
public class ProviderCustomResourceRes {
    
    @Schema(description = "The unique identifier of the resource", example = "workspace-uuid", required = true)
    private String identifier;
    
    @Schema(description = "The display name of the resource", example = "My Workspace", required = true)
    private String displayName;
    
    @Schema(description = "Additional content as JSON", required = false)
    private JsonNode content;

    public ProviderCustomResourceRes() {
    }

    public ProviderCustomResourceRes(String identifier, String displayName, JsonNode content) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.content = content;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }
}


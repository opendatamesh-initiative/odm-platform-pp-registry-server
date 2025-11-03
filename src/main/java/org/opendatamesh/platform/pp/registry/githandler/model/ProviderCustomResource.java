package org.opendatamesh.platform.pp.registry.githandler.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ProviderCustomResource {
    private String identifier;
    private String displayName;
    private JsonNode content;

    public ProviderCustomResource() {
    }

    public ProviderCustomResource(String identifier, String displayName, JsonNode content) {
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


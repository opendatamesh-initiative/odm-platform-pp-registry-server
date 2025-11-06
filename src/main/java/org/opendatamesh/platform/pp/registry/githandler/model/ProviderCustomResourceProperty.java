package org.opendatamesh.platform.pp.registry.githandler.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ProviderCustomResourceProperty {
    private String name;
    private JsonNode value;

    public ProviderCustomResourceProperty() {
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

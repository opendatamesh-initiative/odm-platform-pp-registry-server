package org.opendatamesh.platform.pp.registry.githandler.model;

public class ProviderCustomResourceDefinition {
    private String name;
    private String type;
    private Boolean required;

    public ProviderCustomResourceDefinition() {
    }

    public ProviderCustomResourceDefinition(String name, String type, Boolean required) {
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

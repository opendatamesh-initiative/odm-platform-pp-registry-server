package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

public class RegistryV1VariableResource {
    private Long id;
    private String variableName;
    private String variableValue;

    public RegistryV1VariableResource() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }
}
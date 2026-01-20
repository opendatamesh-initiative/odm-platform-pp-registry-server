package org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "descriptor_variable")
public class DescriptorVariableRes {

    @Schema(description = "The unique identifier of the descriptor variable")
    private Long sequenceId;

    @Schema(description = "The UUID of the data product version this variable belongs to")
    private String dataProductVersionUuid;

    @Schema(description = "The key of the variable")
    private String variableKey;

    @Schema(description = "The value of the variable")
    private String variableValue;

    public DescriptorVariableRes() {
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getDataProductVersionUuid() {
        return dataProductVersionUuid;
    }

    public void setDataProductVersionUuid(String dataProductVersionUuid) {
        this.dataProductVersionUuid = dataProductVersionUuid;
    }

    public String getVariableKey() {
        return variableKey;
    }

    public void setVariableKey(String variableKey) {
        this.variableKey = variableKey;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }
}

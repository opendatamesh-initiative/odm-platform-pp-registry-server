package org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable;

import io.swagger.v3.oas.annotations.Parameter;

public class DescriptorVariableSearchOptions {

    @Parameter(description = "Filter descriptor variables by data product version UUID. Exact match.")
    private String dataProductVersionUuid;

    @Parameter(description = "Filter descriptor variables by variable key. Case-insensitive match.")
    private String variableKey;

    public DescriptorVariableSearchOptions() {
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

}

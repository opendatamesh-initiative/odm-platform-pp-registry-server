package org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;

import java.util.List;

@Schema(name = "StoreDescriptorVariableResultRes", description = "Result resource for storing descriptor variables")
public class StoreDescriptorVariableResultRes {

    @Schema(description = "The list of stored descriptor variables")
    private List<DescriptorVariableRes> descriptorVariables;

    public StoreDescriptorVariableResultRes() {
    }

    public StoreDescriptorVariableResultRes(List<DescriptorVariableRes> descriptorVariables) {
        this.descriptorVariables = descriptorVariables;
    }

    public List<DescriptorVariableRes> getDescriptorVariables() {
        return descriptorVariables;
    }

    public void setDescriptorVariables(List<DescriptorVariableRes> descriptorVariables) {
        this.descriptorVariables = descriptorVariables;
    }
}

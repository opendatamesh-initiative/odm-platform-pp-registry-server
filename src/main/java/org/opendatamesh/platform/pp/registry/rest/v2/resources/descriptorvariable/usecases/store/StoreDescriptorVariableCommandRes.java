package org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;

import java.util.List;

@Schema(name = "StoreDescriptorVariableCommandRes", description = "Command resource for storing descriptor variables")
public class StoreDescriptorVariableCommandRes {

    @Schema(description = "The list of descriptor variables to be stored")
    private List<DescriptorVariableRes> descriptorVariables;

    public StoreDescriptorVariableCommandRes() {
    }

    public List<DescriptorVariableRes> getDescriptorVariables() {
        return descriptorVariables;
    }

    public void setDescriptorVariables(List<DescriptorVariableRes> descriptorVariables) {
        this.descriptorVariables = descriptorVariables;
    }
}

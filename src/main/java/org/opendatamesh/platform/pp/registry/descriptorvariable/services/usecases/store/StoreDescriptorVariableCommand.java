package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;

import java.util.List;

public record StoreDescriptorVariableCommand(List<DescriptorVariable> descriptorVariables) {
}

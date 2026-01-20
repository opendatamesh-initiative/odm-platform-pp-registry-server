package org.opendatamesh.platform.pp.registry.descriptorvariable.services.core;

import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableSearchOptions;
import org.opendatamesh.platform.pp.registry.utils.services.GenericMappedAndFilteredCrudService;

public interface DescriptorVariableCrudService extends GenericMappedAndFilteredCrudService<DescriptorVariableSearchOptions, DescriptorVariableRes, DescriptorVariable, Long> {

}

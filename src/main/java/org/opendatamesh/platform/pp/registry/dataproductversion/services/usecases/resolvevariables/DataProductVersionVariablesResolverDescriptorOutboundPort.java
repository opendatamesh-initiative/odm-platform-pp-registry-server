package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;

import java.util.List;

interface DataProductVersionVariablesResolverDescriptorOutboundPort {
    JsonNode resolveDescriptor(DataProductVersion dataProductVersion, List<DescriptorVariable> descriptorVariables);
}

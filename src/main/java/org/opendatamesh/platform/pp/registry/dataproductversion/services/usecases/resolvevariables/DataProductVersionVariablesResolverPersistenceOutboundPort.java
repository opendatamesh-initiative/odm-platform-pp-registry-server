package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;

import java.util.List;

interface DataProductVersionVariablesResolverPersistenceOutboundPort {
    DataProductVersion findByUuid(String dataProductVersionUuid);

    List<DescriptorVariable> findDescriptorVariables(String dataProductVersionUuid);
}

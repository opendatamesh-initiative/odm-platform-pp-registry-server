package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;

interface StoreDescriptorVariablePersistenceOutboundPort {
    DataProductVersion findDataProductVersionByUuid(String dataProductVersionUuid);

    DescriptorVariable createOrOverride(DescriptorVariable descriptorVariable);
}

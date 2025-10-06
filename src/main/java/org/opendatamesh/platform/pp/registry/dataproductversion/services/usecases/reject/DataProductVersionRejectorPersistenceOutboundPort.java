package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

interface DataProductVersionRejectorPersistenceOutboundPort {
    DataProductVersion findByUuid(String dataProductVersionUuid);

    DataProductVersion save(DataProductVersion dataProductVersion);
}

package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

interface DataProductVersionApproverPersistenceOutboundPort {
    DataProductVersion findByUuid(String dataProductVersionUuid);

    DataProductVersion save(DataProductVersion dataProductVersion);
}

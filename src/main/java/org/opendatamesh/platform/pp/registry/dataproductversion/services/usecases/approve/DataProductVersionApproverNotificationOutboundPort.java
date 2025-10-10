package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

interface DataProductVersionApproverNotificationOutboundPort {
    void emitDataProductVersionPublished(DataProductVersion dataProductVersion);
}

package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

interface DataProductVersionPublisherNotificationOutboundPort {
    void emitDataProductVersionPublicationRequested(DataProductVersion dataProductVersion);
}

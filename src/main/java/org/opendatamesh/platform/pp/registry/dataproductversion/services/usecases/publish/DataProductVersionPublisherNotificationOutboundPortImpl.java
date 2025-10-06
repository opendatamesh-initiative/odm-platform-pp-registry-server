package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

class DataProductVersionPublisherNotificationOutboundPortImpl implements DataProductVersionPublisherNotificationOutboundPort {
    @Override
    public void emitDataProductVersionPublicationRequested(DataProductVersion dataProductVersion) {
        //TODO implement this when porting notifications support
    }
}

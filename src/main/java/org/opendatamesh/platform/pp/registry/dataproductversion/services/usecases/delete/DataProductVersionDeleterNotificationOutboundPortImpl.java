package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

class DataProductVersionDeleterNotificationOutboundPortImpl implements DataProductVersionDeleterNotificationOutboundPort {
    @Override
    public void emitDataProductVersionDeleted(DataProductVersion dataProductVersion) {
        //TODO implement this when porting notifications support
    }
}


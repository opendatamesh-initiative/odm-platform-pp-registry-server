package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;

class DataProductVersionDeleterNotificationOutboundPortImpl implements DataProductVersionDeleterNotificationOutboundPort {
    @Override
    public void emitDataProductVersionDeleted(DataProductVersionShort dataProductVersion) {
        //TODO implement this when porting notifications support
    }
}


package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

class DataProductDeleterNotificationOutboundPortImpl implements DataProductDeleterNotificationOutboundPort {
    @Override
    public void emitDataProductDeleted(DataProduct dataProduct) {
        //TODO implement this when porting notifications support
    }
}


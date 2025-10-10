package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

class DataProductApproverNotificationOutboundPortImpl implements DataProductApproverNotificationOutboundPort {
    @Override
    public void emitDataProductInitialized(DataProduct dataProduct) {
        //TODO implement this when porting notifications support
    }
}

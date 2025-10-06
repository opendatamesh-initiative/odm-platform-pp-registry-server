package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

class DataProductVersionApproverNotificationOutboundPortImpl implements DataProductVersionApproverNotificationOutboundPort {
    @Override
    public void emitDataProductVersionPublished(DataProductVersion dataProductVersion) {
        //TODO implement this when porting notifications support
    }
}

package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approveinitialization;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

public interface DataProductInitializationApproverNotificationOutboundPort {

    void emitDataProductInitializationApproved(DataProduct dataProduct);
}


package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

public interface DataProductDeleterNotificationOutboundPort {

    void emitDataProductDeleted(DataProduct dataProduct);
}


package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

interface DataProductInitializerNotificationOutboundPort {
    void emitDataProductInitializationRequested(DataProduct dataProduct);
}

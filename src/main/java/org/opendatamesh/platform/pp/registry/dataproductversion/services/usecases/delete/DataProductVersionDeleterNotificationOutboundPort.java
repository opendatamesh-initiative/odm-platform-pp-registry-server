package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

public interface DataProductVersionDeleterNotificationOutboundPort {

    void emitDataProductVersionDeleted(DataProductVersion dataProductVersion);
}


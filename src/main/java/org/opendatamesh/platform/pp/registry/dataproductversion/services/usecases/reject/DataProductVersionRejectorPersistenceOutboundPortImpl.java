package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;

class DataProductVersionRejectorPersistenceOutboundPortImpl implements DataProductVersionRejectorPersistenceOutboundPort {

    private final DataProductVersionCrudService dataProductVersionCrudService;

    DataProductVersionRejectorPersistenceOutboundPortImpl(DataProductVersionCrudService dataProductVersionCrudService) {
        this.dataProductVersionCrudService = dataProductVersionCrudService;
    }

    @Override
    public DataProductVersion findByUuid(String dataProductVersionUuid) {
        return dataProductVersionCrudService.findOne(dataProductVersionUuid);
    }

    @Override
    public DataProductVersion save(DataProductVersion dataProductVersion) {
        return dataProductVersionCrudService.overwrite(dataProductVersion.getUuid(), dataProductVersion);
    }
}

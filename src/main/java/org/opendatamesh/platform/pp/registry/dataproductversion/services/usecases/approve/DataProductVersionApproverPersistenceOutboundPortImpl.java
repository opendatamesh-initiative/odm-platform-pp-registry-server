package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;

class DataProductVersionApproverPersistenceOutboundPortImpl implements DataProductVersionApproverPersistenceOutboundPort {

    private final DataProductVersionCrudService dataProductVersionCrudService;

    DataProductVersionApproverPersistenceOutboundPortImpl(DataProductVersionCrudService dataProductVersionCrudService) {
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

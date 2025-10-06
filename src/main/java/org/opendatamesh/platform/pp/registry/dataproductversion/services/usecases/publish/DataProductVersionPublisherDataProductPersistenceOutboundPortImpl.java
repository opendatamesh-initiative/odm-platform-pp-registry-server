package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;

class DataProductVersionPublisherDataProductPersistenceOutboundPortImpl implements DataProductVersionPublisherDataProductPersistenceOutboundPort {

    private final DataProductsService service;

    public DataProductVersionPublisherDataProductPersistenceOutboundPortImpl(DataProductsService service) {
        this.service = service;
    }

    @Override
    public DataProduct findByUuid(String dataProductUuid) {
        return service.findOne(dataProductUuid);
    }
}

package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;

class DataProductRejectorPersistenceOutboundPortImpl implements DataProductRejectorPersistenceOutboundPort {

    private final DataProductsService dataProductsService;

    DataProductRejectorPersistenceOutboundPortImpl(DataProductsService dataProductsService) {
        this.dataProductsService = dataProductsService;
    }

    @Override
    public DataProduct findByUuid(String dataProductUuid) {
        return dataProductsService.findOne(dataProductUuid);
    }

    @Override
    public DataProduct save(DataProduct dataProduct) {
        return dataProductsService.overwrite(dataProduct.getUuid(), dataProduct);
    }
}

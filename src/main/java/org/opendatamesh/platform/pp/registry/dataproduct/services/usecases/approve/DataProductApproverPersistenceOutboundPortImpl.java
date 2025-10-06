package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;

class DataProductApproverPersistenceOutboundPortImpl implements DataProductApproverPersistenceOutboundPort {

    private final DataProductsService dataProductsService;

    DataProductApproverPersistenceOutboundPortImpl(DataProductsService dataProductsService) {
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

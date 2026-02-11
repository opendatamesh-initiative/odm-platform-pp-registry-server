package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;

class DataProductDocumentationFieldsUpdaterPersistenceOutboundPortImpl implements DataProductDocumentationFieldsUpdaterPersistenceOutboundPort {

    private final DataProductsService dataProductsService;

    DataProductDocumentationFieldsUpdaterPersistenceOutboundPortImpl(DataProductsService dataProductsService) {
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

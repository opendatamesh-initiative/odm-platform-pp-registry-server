package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

interface DataProductRejectorPersistenceOutboundPort {
    DataProduct findByUuid(String dataProductUuid);

    DataProduct save(DataProduct dataProduct);
}

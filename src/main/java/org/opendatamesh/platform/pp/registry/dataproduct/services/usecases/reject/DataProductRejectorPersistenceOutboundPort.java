package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

import java.util.Optional;

interface DataProductRejectorPersistenceOutboundPort {
    Optional<DataProduct> find(DataProduct dataProduct);

    DataProduct save(DataProduct dataProduct);
}

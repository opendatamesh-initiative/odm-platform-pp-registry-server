package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

import java.util.Optional;

interface DataProductInitializerPersistenceOutboundPort {
    Optional<DataProduct> find(DataProduct dataProduct);

    void delete(DataProduct dataProduct);

    DataProduct save(DataProduct dataProduct);
}

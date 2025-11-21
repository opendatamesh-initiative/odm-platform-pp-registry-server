package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

import java.util.Optional;

interface DataProductDeleterPersistenceOutboundPort {
    DataProduct findByUuid(String dataProductUuid);

    Optional<DataProduct> findByFqn(String dataProductFqn);

    void delete(DataProduct dataProduct);
}


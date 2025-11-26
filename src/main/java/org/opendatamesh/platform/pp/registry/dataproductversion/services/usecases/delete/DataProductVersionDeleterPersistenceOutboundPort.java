package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;

import java.util.Optional;

interface DataProductVersionDeleterPersistenceOutboundPort {
    DataProductVersionShort findByUuid(String dataProductVersionUuid);

    Optional<DataProductVersionShort> findByDataProductUuidAndTag(String dataProductUuid, String tag);

    Optional<DataProduct> findDataProductByFqn(String dataProductFqn);

    void delete(DataProductVersionShort dataProductVersion);
}


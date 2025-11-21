package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

import java.util.Optional;

interface DataProductVersionDeleterPersistenceOutboundPort {
    DataProductVersion findByUuid(String dataProductVersionUuid);

    Optional<DataProductVersion> findByDataProductUuidAndTag(String dataProductUuid, String tag);

    void delete(DataProductVersion dataProductVersion);
}


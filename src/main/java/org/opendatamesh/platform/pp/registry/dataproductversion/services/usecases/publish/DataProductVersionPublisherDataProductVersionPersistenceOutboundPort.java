package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;

import java.util.Optional;

interface DataProductVersionPublisherDataProductVersionPersistenceOutboundPort {

    Optional<DataProductVersionShort> findByDataProductUuidAndVersionNumber(String dataProductUuid, String versionNumber);

    Optional<DataProductVersionShort> findLatestByDataProductUuidExcludingUuid(String dataProductUuid, String excludeUuid);

    DataProductVersion findByUuid(String dataProductVersionUuid);

    void delete(String dataProductVersionUuid);

    DataProductVersion save(DataProductVersion dataProductVersion);
}

package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

interface DataProductVersionDocumentationFieldsUpdaterPersistenceOutboundPort {
    DataProductVersion findByUuid(String dataProductVersionUuid);

    DataProductVersion save(DataProductVersion dataProductVersion);
}

package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.updatefields;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

interface DataProductFieldsUpdaterPersistenceOutboundPort {

    DataProduct findByUuid(String dataProductUuid);

    DataProduct save(DataProduct dataProduct);
}

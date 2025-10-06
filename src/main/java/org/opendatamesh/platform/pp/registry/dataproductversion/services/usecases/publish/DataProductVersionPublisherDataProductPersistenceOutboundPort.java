package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

interface DataProductVersionPublisherDataProductPersistenceOutboundPort {

    DataProduct findByUuid(String dataProductUuid);
}

package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

import java.util.Optional;

public interface DataProductApproverPersistenceOutboundPort {

    Optional<DataProduct> find(DataProduct dataProduct);

    DataProduct save(DataProduct dataProduct);
}

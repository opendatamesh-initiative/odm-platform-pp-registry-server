package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

class DataProductRejectorPersistenceOutboundPortImpl implements DataProductRejectorPersistenceOutboundPort {

    private final DataProductsService dataProductsService;

    DataProductRejectorPersistenceOutboundPortImpl(DataProductsService dataProductsService) {
        this.dataProductsService = dataProductsService;
    }

    @Override
    public Optional<DataProduct> find(DataProduct dataProduct) {
        DataProductSearchOptions filter = new DataProductSearchOptions();
        filter.setFqn(dataProduct.getFqn());
        return dataProductsService.findAllFiltered(Pageable.ofSize(1), filter).stream().findFirst();
    }

    @Override
    public DataProduct save(DataProduct dataProduct) {
        return dataProductsService.overwrite(dataProduct.getUuid(), dataProduct);
    }
}

package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

class DataProductDeleterPersistenceOutboundPortImpl implements DataProductDeleterPersistenceOutboundPort {

    private final DataProductsService dataProductsService;

    DataProductDeleterPersistenceOutboundPortImpl(DataProductsService dataProductsService) {
        this.dataProductsService = dataProductsService;
    }

    @Override
    public DataProduct findByUuid(String dataProductUuid) {
        return dataProductsService.findOne(dataProductUuid);
    }

    @Override
    public Optional<DataProduct> findByFqn(String dataProductFqn) {
        DataProductSearchOptions filter = new DataProductSearchOptions();
        filter.setFqn(dataProductFqn);
        return dataProductsService.findAllFiltered(Pageable.ofSize(1), filter).stream().findFirst();
    }

    @Override
    public void delete(DataProduct dataProduct) {
        dataProductsService.delete(dataProduct.getUuid());
    }
}


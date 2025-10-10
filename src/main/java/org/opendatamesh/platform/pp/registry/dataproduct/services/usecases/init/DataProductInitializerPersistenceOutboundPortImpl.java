package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

class DataProductInitializerPersistenceOutboundPortImpl implements DataProductInitializerPersistenceOutboundPort {

    private final DataProductsService dataProductsService;

    DataProductInitializerPersistenceOutboundPortImpl(DataProductsService dataProductsService) {
        this.dataProductsService = dataProductsService;
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

    @Override
    public DataProduct save(DataProduct dataProduct) {
        return dataProductsService.create(dataProduct);
    }
}

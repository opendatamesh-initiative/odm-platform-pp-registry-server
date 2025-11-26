package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

class DataProductVersionDeleterPersistenceOutboundPortImpl implements DataProductVersionDeleterPersistenceOutboundPort {

    private final DataProductVersionCrudService dataProductVersionCrudService;
    private final DataProductVersionsQueryService dataProductVersionsQueryService;
    private final DataProductsService dataProductsService;

    DataProductVersionDeleterPersistenceOutboundPortImpl(
            DataProductVersionCrudService dataProductVersionCrudService,
            DataProductVersionsQueryService dataProductVersionsQueryService,
            DataProductsService dataProductsService) {
        this.dataProductVersionCrudService = dataProductVersionCrudService;
        this.dataProductVersionsQueryService = dataProductVersionsQueryService;
        this.dataProductsService = dataProductsService;
    }

    @Override
    public DataProductVersionShort findByUuid(String dataProductVersionUuid) {
        return dataProductVersionsQueryService.findOne(dataProductVersionUuid);
    }

    @Override
    public Optional<DataProductVersionShort> findByDataProductUuidAndTag(String dataProductUuid, String tag) {
        DataProductVersionSearchOptions filter = new DataProductVersionSearchOptions();
        filter.setDataProductUuid(dataProductUuid);
        filter.setTag(tag);
        // Find all with filters not supported by crud service, but supported by query service
        return dataProductVersionsQueryService.findAllShort(Pageable.ofSize(1), filter)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<DataProduct> findDataProductByFqn(String dataProductFqn) {
        DataProductSearchOptions filter = new DataProductSearchOptions();
        filter.setFqn(dataProductFqn);
        return dataProductsService.findAllFiltered(Pageable.ofSize(1), filter)
                .stream()
                .findFirst();
    }

    @Override
    public void delete(DataProductVersionShort dataProductVersion) {
        dataProductVersionCrudService.delete(dataProductVersion.getUuid());
    }
}


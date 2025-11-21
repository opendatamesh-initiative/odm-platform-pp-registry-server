package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

class DataProductVersionDeleterPersistenceOutboundPortImpl implements DataProductVersionDeleterPersistenceOutboundPort {

    private final DataProductVersionCrudService dataProductVersionCrudService;
    private final DataProductVersionsQueryService dataProductVersionsQueryService;

    DataProductVersionDeleterPersistenceOutboundPortImpl(
            DataProductVersionCrudService dataProductVersionCrudService,
            DataProductVersionsQueryService dataProductVersionsQueryService) {
        this.dataProductVersionCrudService = dataProductVersionCrudService;
        this.dataProductVersionsQueryService = dataProductVersionsQueryService;
    }

    @Override
    public DataProductVersion findByUuid(String dataProductVersionUuid) {
        return dataProductVersionCrudService.findOne(dataProductVersionUuid);
    }

    @Override
    public Optional<DataProductVersion> findByDataProductUuidAndTag(String dataProductUuid, String tag) {
        DataProductVersionSearchOptions filter = new DataProductVersionSearchOptions();
        filter.setDataProductUuid(dataProductUuid);
        filter.setTag(tag);
        // Find all with filters not supported by crud service, but supported by query service
        return dataProductVersionsQueryService.findAllShort(Pageable.ofSize(1), filter)
                .stream()
                .findFirst()
                .map(shortVersion -> dataProductVersionCrudService.findOne(shortVersion.getUuid()));
    }

    @Override
    public void delete(DataProductVersion dataProductVersion) {
        dataProductVersionCrudService.delete(dataProductVersion.getUuid());
    }
}


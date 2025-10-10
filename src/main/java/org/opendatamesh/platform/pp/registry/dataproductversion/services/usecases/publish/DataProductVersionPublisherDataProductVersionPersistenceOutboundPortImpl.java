package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

class DataProductVersionPublisherDataProductVersionPersistenceOutboundPortImpl implements DataProductVersionPublisherDataProductVersionPersistenceOutboundPort {

    private final DataProductVersionCrudService dataProductVersionCrudService;
    private final DataProductVersionsQueryService dataProductVersionsQueryService;

    DataProductVersionPublisherDataProductVersionPersistenceOutboundPortImpl(DataProductVersionCrudService dataProductVersionCrudService, DataProductVersionsQueryService dataProductVersionsQueryService) {
        this.dataProductVersionCrudService = dataProductVersionCrudService;
        this.dataProductVersionsQueryService = dataProductVersionsQueryService;
    }

    @Override
    public Optional<DataProductVersionShort> findByDataProductUuidAndTag(String dataProductUuid, String tag) {
        DataProductVersionSearchOptions filter = new DataProductVersionSearchOptions();
        filter.setDataProductUuid(dataProductUuid);
        filter.setTag(tag);
        return dataProductVersionsQueryService.findAllShort(Pageable.ofSize(1), filter).stream().findFirst();
    }

    @Override
    public void delete(String dataProductVersionUuid) {
        dataProductVersionCrudService.delete(dataProductVersionUuid);
    }

    @Override
    public DataProductVersion save(DataProductVersion dataProductVersion) {
        return dataProductVersionCrudService.create(dataProductVersion);
    }
}

package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

class DataProductVersionPublisherDataProductVersionPersistenceOutboundPortImpl implements DataProductVersionPublisherDataProductVersionPersistenceOutboundPort {

    private final DataProductVersionCrudService dataProductVersionCrudService;
    private final DataProductVersionsQueryService dataProductVersionsQueryService;

    DataProductVersionPublisherDataProductVersionPersistenceOutboundPortImpl(DataProductVersionCrudService dataProductVersionCrudService, DataProductVersionsQueryService dataProductVersionsQueryService) {
        this.dataProductVersionCrudService = dataProductVersionCrudService;
        this.dataProductVersionsQueryService = dataProductVersionsQueryService;
    }

    @Override
    public Optional<DataProductVersionShort> findByDataProductUuidAndVersionNumber(String dataProductUuid, String versionNumber) {
        DataProductVersionSearchOptions filter = new DataProductVersionSearchOptions();
        filter.setDataProductUuid(dataProductUuid);
        filter.setVersionNumber(versionNumber);
        return dataProductVersionsQueryService.findAllShort(Pageable.ofSize(1), filter).stream().findFirst();
    }

    @Override
    public Optional<DataProductVersionShort> findLatestByDataProductUuidExcludingUuid(String dataProductUuid, String excludeUuid) {
        DataProductVersionSearchOptions filter = new DataProductVersionSearchOptions();
        filter.setDataProductUuid(dataProductUuid);
        // Request 2 results: the first will be the current version (to exclude), the second will be the previous version
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        return dataProductVersionsQueryService.findAllShort(pageable, filter)
                .stream()
                .filter(version -> !version.getUuid().equals(excludeUuid))
                .findFirst();
    }

    @Override
    public DataProductVersion findByUuid(String dataProductVersionUuid) {
        return dataProductVersionCrudService.findOne(dataProductVersionUuid);
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

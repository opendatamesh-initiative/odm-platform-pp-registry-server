package org.opendatamesh.platform.pp.registry.dataproductversion.services.core;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.dataproductversion.repositories.DataProductVersionsShortRepository;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionShortRes;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link DataProductVersionsQueryService} for querying multiple DataProductVersion entities.
 * <p>
 * This service implementation is optimized for read operations involving multiple entities,
 * using the lightweight DataProductVersionShort entity to exclude descriptor content for better performance.
 */
@Service
public class DataProductVersionsQueryServiceImpl implements DataProductVersionsQueryService {

    private final DataProductVersionMapper mapper;
    private final DataProductVersionsShortRepository repository;

    @Autowired
    public DataProductVersionsQueryServiceImpl(DataProductVersionMapper mapper,
                                               DataProductVersionsShortRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Page<DataProductVersionShortRes> findAllResourcesShort(Pageable pageable, DataProductVersionSearchOptions searchOptions) {
        return findAllShort(pageable, searchOptions)
                .map(mapper::toShortResFromShort);
    }

    @Override
    public Page<DataProductVersionShort> findAllShort(Pageable pageable, DataProductVersionSearchOptions searchOptions) {
        Specification<DataProductVersionShort> spec = getSpecFromFilters(searchOptions);
        return repository.findAll(spec, pageable);
    }

    private Specification<DataProductVersionShort> getSpecFromFilters(DataProductVersionSearchOptions searchOptions) {
        List<Specification<DataProductVersionShort>> specs = new ArrayList<>();

        if (searchOptions != null) {
            if (StringUtils.hasText(searchOptions.getDataProductUuid())) {
                specs.add(DataProductVersionsShortRepository.Specs.hasDataProductUuid(searchOptions.getDataProductUuid()));
            }
            if (StringUtils.hasText(searchOptions.getName())) {
                specs.add(DataProductVersionsShortRepository.Specs.hasName(searchOptions.getName()));
            }
            if (StringUtils.hasText(searchOptions.getTag())) {
                specs.add(DataProductVersionsShortRepository.Specs.hasTag(searchOptions.getTag()));
            }
            if (searchOptions.getValidationState() != null) {
                DataProductVersionValidationState validationState = DataProductVersionValidationState.valueOf(searchOptions.getValidationState().name());
                specs.add(DataProductVersionsShortRepository.Specs.hasValidationState(validationState));
            }
            if (searchOptions.getSearch() != null) {
                specs.add(DataProductVersionsShortRepository.Specs.matchSearch(searchOptions.getSearch()));
            }
        }

        return SpecsUtils.combineWithAnd(specs);
    }
}

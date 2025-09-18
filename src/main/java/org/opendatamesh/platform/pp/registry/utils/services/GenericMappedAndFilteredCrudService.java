package org.opendatamesh.platform.pp.registry.utils.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

public interface GenericMappedAndFilteredCrudService<F, R, T, ID extends Serializable> extends GenericMappedCrudService<R, T, ID> {
    Page<R> findAllResourcesFiltered(Pageable pageable, F filters);

    Page<T> findAllFiltered(Pageable pageable, F filters);
}

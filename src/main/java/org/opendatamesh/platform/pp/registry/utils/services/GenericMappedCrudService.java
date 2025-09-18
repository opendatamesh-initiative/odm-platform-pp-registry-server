package org.opendatamesh.platform.pp.registry.utils.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

public interface GenericMappedCrudService<R, T, ID extends Serializable> extends GenericCrudService<T, ID> {
    Page<R> findAllResources(Pageable pageable);

    R findOneResource(ID identifier);

    R createResource(R objectToCreate);

    R overwriteResource(ID identifier, R objectToOverwrite);

    R deleteReturningResource(ID identifier);
}

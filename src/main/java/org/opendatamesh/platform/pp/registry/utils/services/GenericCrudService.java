package org.opendatamesh.platform.pp.registry.utils.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.function.Function;

public interface GenericCrudService<T, ID extends Serializable> {
    Page<T> findAll(Pageable pageable);

    T findOne(ID identifier);

    void checkExistenceOrThrow(ID identifier);

    T create(T objectToCreate);

    T overwrite(ID identifier, T objectToOverwrite);

    void delete(ID identifier);

    <R> R deleteReturning(ID identifier, Function<T, R> mapper);
}

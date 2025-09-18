package org.opendatamesh.platform.pp.registry.utils.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

public abstract class GenericMappedCrudServiceImpl<R, T, ID extends Serializable> extends GenericCrudServiceImpl<T, ID> implements GenericMappedCrudService<R, T, ID> {

    @Autowired
    private TransactionHandler transactionHandler;

    protected abstract R toRes(T entity);

    protected abstract T toEntity(R resource);

    @Override
    public Page<R> findAllResources(Pageable pageable) {
        return transactionHandler.runInTransaction(() -> {
            Page<T> entitiesPage = findAll(pageable);
            return entitiesPage.map(this::toRes);
        });
    }

    @Override
    public R findOneResource(ID identifier) {
        return transactionHandler.runInTransaction(() -> {
            T entity = findOne(identifier);
            return toRes(entity);
        });
    }

    @Override
    public R createResource(R objectToCreate) {
        T entityToCreate = toEntity(objectToCreate);
        T createdEntity = create(entityToCreate);
        return toRes(createdEntity);
    }

    @Override
    public R overwriteResource(ID identifier, R objectToOverwrite) {
        T entityToOverwrite = toEntity(objectToOverwrite);
        T overwrittenEntity = overwrite(identifier, entityToOverwrite);

        return toRes(overwrittenEntity);
    }

    @Override
    public R deleteReturningResource(ID identifier) {
        return deleteReturning(identifier, this::toRes);
    }

}

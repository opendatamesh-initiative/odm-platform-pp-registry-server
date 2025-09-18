package org.opendatamesh.platform.pp.registry.utils.services;

import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;

public abstract class GenericMappedAndFilteredCrudServiceImpl<F, R, T, ID extends Serializable> extends GenericMappedCrudServiceImpl<R, T, ID> implements GenericMappedAndFilteredCrudService<F, R, T, ID> {

    @Autowired
    private TransactionHandler transactionHandler;

    @Override
    public  Page<R> findAllResourcesFiltered(Pageable pageable, F filters) {
        return transactionHandler.runInTransaction(() ->
                findAllFiltered(pageable, filters).map(this::toRes)
        );
    }

    @Override
    public  Page<T> findAllFiltered(Pageable pageable, F filters) {
        Specification<T> spec = getSpecFromFilters(filters);
        return getRepository().findAll(spec, pageable);
    }

    protected abstract PagingAndSortingAndSpecificationExecutorRepository<T, ID> getRepository();


    protected abstract Specification<T> getSpecFromFilters(F filters);

}

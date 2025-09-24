package org.opendatamesh.platform.pp.registry.utils.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

@NoRepositoryBean
public interface PagingAndSortingAndSpecificationExecutorRepository<T, ID extends Serializable>
        extends CrudRepository<T, ID>, PagingAndSortingRepository<T, ID>, JpaSpecificationExecutor<T> {
}

package org.opendatamesh.platform.pp.registry.dataproduct.repositories;


import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DataProductsRepository extends PagingAndSortingRepository<DataProduct, String> {
}

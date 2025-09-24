package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.repositories.DataProductsRepository;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.utils.services.GenericCrudServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;


@Service
public class DataProductServiceImpl extends GenericCrudServiceImpl<DataProduct, String> implements DataProductService {

    @Autowired
    GitProviderFactory gitProviderFactory;

    @Autowired
    private DataProductsRepository repository;

    @Override
    protected PagingAndSortingRepository<DataProduct, String> getRepository() {
        return repository;
    }

    @Override
    protected void validate(DataProduct objectToValidate) {

    }

    @Override
    protected void reconcile(DataProduct objectToReconcile) {

    }
}

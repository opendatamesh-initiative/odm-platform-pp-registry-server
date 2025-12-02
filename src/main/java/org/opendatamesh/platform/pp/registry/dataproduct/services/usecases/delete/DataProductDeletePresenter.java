package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

public interface DataProductDeletePresenter {

    void presentDataProductDeleted(DataProduct dataProduct);
}


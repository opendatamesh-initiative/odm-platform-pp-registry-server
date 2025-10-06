package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

public interface DataProductRejectPresenter {

    void presentDataProductRejected(DataProduct dataProduct);
}

package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

public interface DataProductApprovePresenter {

    void presentDataProductApproved(DataProduct dataProduct);
}

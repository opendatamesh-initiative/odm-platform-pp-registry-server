package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

public interface DataProductVersionApprovePresenter {

    void presentDataProductVersionApproved(DataProductVersion dataProductVersion);
}

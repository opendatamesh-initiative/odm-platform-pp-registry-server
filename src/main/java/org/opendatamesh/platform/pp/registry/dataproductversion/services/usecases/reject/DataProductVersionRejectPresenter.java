package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

public interface DataProductVersionRejectPresenter {

    void presentDataProductVersionRejected(DataProductVersion dataProductVersion);
}

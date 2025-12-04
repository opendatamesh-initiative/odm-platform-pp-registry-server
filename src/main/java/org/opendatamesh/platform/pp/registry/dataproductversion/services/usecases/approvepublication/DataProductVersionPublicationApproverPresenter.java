package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approvepublication;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

public interface DataProductVersionPublicationApproverPresenter {

    void presentDataProductVersionInitializationApproved(DataProductVersion dataProductVersion);
}


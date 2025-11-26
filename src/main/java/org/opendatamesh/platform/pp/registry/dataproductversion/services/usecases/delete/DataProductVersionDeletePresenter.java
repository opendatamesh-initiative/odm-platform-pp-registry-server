package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;

public interface DataProductVersionDeletePresenter {

    void presentDataProductVersionDeleted(DataProductVersionShort dataProductVersion);
}


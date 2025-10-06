package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionRejectCommandRes", description = "Response resource for data product version rejection command")
public class DataProductVersionRejectCommandRes {
    
    @Schema(description = "The data product version to be rejected")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionRejectCommandRes() {
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

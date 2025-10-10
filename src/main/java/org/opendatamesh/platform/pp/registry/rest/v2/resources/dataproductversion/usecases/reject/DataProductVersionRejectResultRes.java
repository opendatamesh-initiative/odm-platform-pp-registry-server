package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionRejectResultRes", description = "Response resource for data product version rejection result")
public class DataProductVersionRejectResultRes {
    
    @Schema(description = "The rejected data product version")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionRejectResultRes() {
    }

    public DataProductVersionRejectResultRes(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

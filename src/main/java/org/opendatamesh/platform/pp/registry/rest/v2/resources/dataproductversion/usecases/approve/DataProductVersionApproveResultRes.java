package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionApproveResultRes", description = "Response resource for data product version approval result")
public class DataProductVersionApproveResultRes {
    
    @Schema(description = "The approved data product version")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionApproveResultRes() {
    }

    public DataProductVersionApproveResultRes(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

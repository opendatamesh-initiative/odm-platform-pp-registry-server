package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionApproveCommandRes", description = "Response resource for data product version approval command")
public class DataProductVersionApproveCommandRes {
    
    @Schema(description = "The data product version to be approved")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionApproveCommandRes() {
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

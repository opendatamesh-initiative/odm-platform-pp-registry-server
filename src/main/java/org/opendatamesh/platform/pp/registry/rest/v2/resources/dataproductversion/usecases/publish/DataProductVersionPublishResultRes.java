package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionPublishResultRes", description = "Response resource for data product version publication result")
public class DataProductVersionPublishResultRes {
    
    @Schema(description = "The published data product version")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionPublishResultRes() {
    }

    public DataProductVersionPublishResultRes(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

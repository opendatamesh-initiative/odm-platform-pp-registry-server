package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionPublishCommandRes", description = "Response resource for data product version publication command")
public class DataProductVersionPublishCommandRes {
    
    @Schema(description = "The data product version to be published")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionPublishCommandRes() {
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

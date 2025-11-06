package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionDocumentationFieldsUpdateCommandRes", description = "Response resource for data product version updating documentation fields command")

public class DataProductVersionDocumentationFieldsUpdateCommandRes {

    @Schema(description = "The data product version to be updated with documentation fields")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionDocumentationFieldsUpdateCommandRes() {
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

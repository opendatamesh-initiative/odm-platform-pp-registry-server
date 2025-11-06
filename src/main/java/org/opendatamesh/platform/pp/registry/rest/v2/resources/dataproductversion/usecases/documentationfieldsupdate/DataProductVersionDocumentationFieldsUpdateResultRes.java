package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

@Schema(name = "DataProductVersionDocumentationFieldsUpdateResultRes", description = "Response resource for data product version documentation fields update result")
public class DataProductVersionDocumentationFieldsUpdateResultRes {

    @Schema(description = "The updated data product version")
    private DataProductVersionRes dataProductVersion;

    public DataProductVersionDocumentationFieldsUpdateResultRes() {
    }

    public DataProductVersionDocumentationFieldsUpdateResultRes(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}

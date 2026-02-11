package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.updatefields;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DataProductFieldsUpdateCommandRes", description = "Command resource for data product fields update")
public class DataProductDocumentationFieldsUpdateCommandRes {

    @Schema(description = "The data product to be updated with the provided fields", required = true)
    private DataProductFieldsRes dataProduct;

    public DataProductDocumentationFieldsUpdateCommandRes() {
    }

    public DataProductFieldsRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductFieldsRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

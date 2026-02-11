package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.updatefields;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DataProductFieldsUpdateCommandRes", description = "Command resource for data product fields update")
public class DataProductDocumentationFieldsUpdateCommandRes {

    @Schema(description = "The data product to be updated with the provided fields", required = true)
    private DataProductDocumentationFieldsRes dataProduct;

    public DataProductDocumentationFieldsUpdateCommandRes() {
    }

    public DataProductDocumentationFieldsRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductDocumentationFieldsRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

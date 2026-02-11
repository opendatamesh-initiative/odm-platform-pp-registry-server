package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.updatefields;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

@Schema(name = "DataProductFieldsUpdateResultRes", description = "Response resource for data product fields update result")
public class DataProductDocumentationFieldsUpdateResultRes {

    @Schema(description = "The updated data product")
    private DataProductRes dataProduct;

    public DataProductDocumentationFieldsUpdateResultRes() {
    }

    public DataProductDocumentationFieldsUpdateResultRes(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

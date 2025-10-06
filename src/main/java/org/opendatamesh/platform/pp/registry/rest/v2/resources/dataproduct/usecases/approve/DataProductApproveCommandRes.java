package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

@Schema(name = "DataProductApproveCommandRes", description = "Response resource for data product approval command")
public class DataProductApproveCommandRes {
    
    @Schema(description = "The data product to approve")
    private DataProductRes dataProduct;

    public DataProductApproveCommandRes() {
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

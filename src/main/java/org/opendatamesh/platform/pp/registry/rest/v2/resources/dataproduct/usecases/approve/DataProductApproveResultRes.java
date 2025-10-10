package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

@Schema(name = "DataProductApproveResultRes", description = "Response resource for data product approval result")
public class DataProductApproveResultRes {
    
    @Schema(description = "The approved data product")
    private DataProductRes dataProduct;

    public DataProductApproveResultRes() {
    }

    public DataProductApproveResultRes(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

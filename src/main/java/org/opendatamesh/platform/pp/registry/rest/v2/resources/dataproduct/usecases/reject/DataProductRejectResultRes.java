package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

@Schema(name = "DataProductRejectResultRes", description = "Response resource for data product rejection result")
public class DataProductRejectResultRes {
    
    @Schema(description = "The rejected data product")
    private DataProductRes dataProduct;

    public DataProductRejectResultRes() {
    }

    public DataProductRejectResultRes(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

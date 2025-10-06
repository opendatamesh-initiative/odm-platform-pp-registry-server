package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

@Schema(name = "DataProductRejectCommandRes", description = "Response resource for data product rejection command")
public class DataProductRejectCommandRes {
    
    @Schema(description = "The data product to reject")
    private DataProductRes dataProduct;

    public DataProductRejectCommandRes() {
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

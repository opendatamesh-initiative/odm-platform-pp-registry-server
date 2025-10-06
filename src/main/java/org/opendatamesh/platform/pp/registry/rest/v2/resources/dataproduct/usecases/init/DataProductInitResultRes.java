package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

@Schema(name = "DataProductInitResultRes", description = "Response resource for data product initialization result")
public class DataProductInitResultRes {
    
    @Schema(description = "The initialized data product")
    private DataProductRes dataProduct;

    public DataProductInitResultRes() {
    }

    public DataProductInitResultRes(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

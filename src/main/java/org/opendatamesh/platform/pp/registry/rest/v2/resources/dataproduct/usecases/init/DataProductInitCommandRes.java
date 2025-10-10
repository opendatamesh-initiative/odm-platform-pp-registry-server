package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

@Schema(name = "DataProductInitCommandRes", description = "Response resource for data product initialization command")
public class DataProductInitCommandRes {
    
    @Schema(description = "The initialized data product information")
    private DataProductRes dataProduct;

    public DataProductInitCommandRes() {
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}

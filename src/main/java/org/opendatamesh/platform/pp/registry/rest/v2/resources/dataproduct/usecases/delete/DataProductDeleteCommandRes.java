package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.delete;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DataProductDeleteCommandRes", description = "Request resource for data product deletion command")
public class DataProductDeleteCommandRes {
    
    @Schema(description = "The UUID of the data product to delete")
    private String dataProductUuid;
    
    @Schema(description = "The fully qualified name (FQN) of the data product to delete")
    private String dataProductFqn;

    public DataProductDeleteCommandRes() {
    }

    public String getDataProductUuid() {
        return dataProductUuid;
    }

    public void setDataProductUuid(String dataProductUuid) {
        this.dataProductUuid = dataProductUuid;
    }

    public String getDataProductFqn() {
        return dataProductFqn;
    }

    public void setDataProductFqn(String dataProductFqn) {
        this.dataProductFqn = dataProductFqn;
    }
}


package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.delete;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DataProductVersionDeleteCommandRes", description = "Request resource for data product version deletion command")
public class DataProductVersionDeleteCommandRes {
    
    @Schema(description = "The UUID of the data product version to delete")
    private String dataProductVersionUuid;
    
    @Schema(description = "The fully qualified name (FQN) of the data product")
    private String dataProductFqn;
    
    @Schema(description = "The tag of the data product version")
    private String dataProductVersionTag;

    public DataProductVersionDeleteCommandRes() {
    }

    public String getDataProductVersionUuid() {
        return dataProductVersionUuid;
    }

    public void setDataProductVersionUuid(String dataProductVersionUuid) {
        this.dataProductVersionUuid = dataProductVersionUuid;
    }

    public String getDataProductFqn() {
        return dataProductFqn;
    }

    public void setDataProductFqn(String dataProductFqn) {
        this.dataProductFqn = dataProductFqn;
    }

    public String getDataProductVersionTag() {
        return dataProductVersionTag;
    }

    public void setDataProductVersionTag(String dataProductVersionTag) {
        this.dataProductVersionTag = dataProductVersionTag;
    }
}


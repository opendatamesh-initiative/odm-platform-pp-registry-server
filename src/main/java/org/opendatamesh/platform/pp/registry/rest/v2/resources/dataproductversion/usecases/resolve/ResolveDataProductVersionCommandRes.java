package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.resolve;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResolveDataProductVersionCommandRes", description = "Command resource for resolving variables in a data product version")
public class ResolveDataProductVersionCommandRes {

    @Schema(description = "The UUID of the data product version to resolve", required = true)
    private String dataProductVersionUuid;

    public ResolveDataProductVersionCommandRes() {
    }

    public ResolveDataProductVersionCommandRes(String dataProductVersionUuid) {
        this.dataProductVersionUuid = dataProductVersionUuid;
    }

    public String getDataProductVersionUuid() {
        return dataProductVersionUuid;
    }

    public void setDataProductVersionUuid(String dataProductVersionUuid) {
        this.dataProductVersionUuid = dataProductVersionUuid;
    }
}

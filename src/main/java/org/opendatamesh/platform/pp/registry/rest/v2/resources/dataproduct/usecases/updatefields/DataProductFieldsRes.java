package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.updatefields;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRepoRes;

@Schema(name = "DataProductFieldsRes", description = "Data product fields that can be updated")
public class DataProductFieldsRes {

    @Schema(description = "The unique identifier of the data product", required = true)
    private String uuid;

    @Schema(description = "The display name of the data product")
    private String displayName;

    @Schema(description = "The description of the data product")
    private String description;

    @Schema(description = "The data product repository (replaces the whole object when provided)")
    private DataProductRepoRes dataProductRepo;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataProductRepoRes getDataProductRepo() {
        return dataProductRepo;
    }

    public void setDataProductRepo(DataProductRepoRes dataProductRepo) {
        this.dataProductRepo = dataProductRepo;
    }
}

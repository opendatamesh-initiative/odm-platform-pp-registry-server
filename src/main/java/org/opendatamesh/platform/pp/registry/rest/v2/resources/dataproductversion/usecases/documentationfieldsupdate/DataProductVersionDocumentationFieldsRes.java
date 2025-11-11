package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.utils.resources.VersionedRes;

@Schema(name = "data_product_versions_documentation_fields")
public class DataProductVersionDocumentationFieldsRes extends VersionedRes {

    @Schema(description = "The unique identifier of the data product version")
    private String uuid;

    @Schema(description = "The name of the data product version")
    private String name;

    @Schema(description = "The description of the data product version")
    private String description;

    @Schema(description = "The user id who last updated the data product version")
    private String updatedBy;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

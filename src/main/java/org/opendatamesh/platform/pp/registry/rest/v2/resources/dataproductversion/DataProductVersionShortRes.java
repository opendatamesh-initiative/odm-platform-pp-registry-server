package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.utils.resources.VersionedRes;

@Schema(name = "data_product_versions_short")
public class DataProductVersionShortRes extends VersionedRes {

    @Schema(description = "The unique identifier of the data product version")
    private String uuid;

    @Schema(description = "The UUID of the parent data product")
    private String dataProductUuid;

    @Schema(description = "The name of the data product version")
    private String name;

    @Schema(description = "The description of the data product version")
    private String description;

    @Schema(description = "The tag of the data product version")
    private String tag;

    @Schema(description = "The descriptor version number")
    private String versionNumber;

    @Schema(description = "The validation state of the data product version")
    private DataProductVersionValidationState validationState;
    
    @Schema(description = "The user id who created the data product version")
    private String createdBy;

    @Schema(description = "The user id who last updated the data product version")
    private String updatedBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

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

    public String getDataProductUuid() {
        return dataProductUuid;
    }

    public void setDataProductUuid(String dataProductUuid) {
        this.dataProductUuid = dataProductUuid;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public DataProductVersionValidationState getValidationState() {
        return validationState;
    }

    public void setValidationState(DataProductVersionValidationState validationState) {
        this.validationState = validationState;
    }
}

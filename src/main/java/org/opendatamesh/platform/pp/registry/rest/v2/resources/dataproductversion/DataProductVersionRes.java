package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.utils.resources.VersionedRes;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "data_product_versions")
public class DataProductVersionRes extends VersionedRes {

    @Schema(description = "The unique identifier of the data product version")
    private String uuid;

    @Schema(description = "The parent data product details")
    private DataProductRes dataProduct;

    @Schema(description = "The name of the data product version")
    private String name;

    @Schema(description = "The description of the data product version")
    private String description;

    @Schema(description = "The tag of the data product version")
    private String tag;

    @Schema(description = "The validation state of the data product version")
    private DataProductVersionValidationStateRes validationState;

    @Schema(description = "The descriptor specification")
    private String spec;

    @Schema(description = "The descriptor specification version")
    private String specVersion;

    @Schema(description = "The descriptor version")
    private String versionNumber;

    @Schema(description = "The descriptor content")
    private JsonNode content;

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

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
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

    public DataProductVersionValidationStateRes getValidationState() {
        return validationState;
    }

    public void setValidationState(DataProductVersionValidationStateRes validationState) {
        this.validationState = validationState;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }
}

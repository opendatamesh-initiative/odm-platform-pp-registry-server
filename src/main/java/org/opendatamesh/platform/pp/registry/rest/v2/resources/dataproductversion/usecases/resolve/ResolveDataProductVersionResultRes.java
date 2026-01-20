package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.resolve;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionValidationStateRes;

@Schema(name = "ResolveDataProductVersionResultRes", description = "Result resource for resolving variables in a data product version")
public class ResolveDataProductVersionResultRes {

    @Schema(description = "The data product version with resolved variables in the content")
    private ResolvedDataProductVersionRes dataProductVersion;

    public ResolveDataProductVersionResultRes() {
    }

    public ResolveDataProductVersionResultRes(ResolvedDataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public ResolvedDataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(ResolvedDataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public static class ResolvedDataProductVersionRes {
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

        @Schema(description = "The descriptor content containing resolved variables")
        private JsonNode resolvedContent;

        @Schema(description = "The user id who created the data product version")
        private String createdBy;

        @Schema(description = "The user id who last updated the data product version")
        private String updatedBy;

        public ResolvedDataProductVersionRes() {
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

        public JsonNode getResolvedContent() {
            return resolvedContent;
        }

        public void setResolvedContent(JsonNode resolvedContent) {
            this.resolvedContent = resolvedContent;
        }

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
    }
}

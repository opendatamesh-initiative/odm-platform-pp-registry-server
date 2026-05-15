package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public class DataProductVersionSearchOptions {

    @Parameter(
            description = "Filter data product versions by data product UUID. Exact match.",
            schema = @Schema(type = "string")
    )
    private String dataProductUuid;

    @Parameter(
            description = "Filter data product versions by name. Exact match (case-insensitive).",
            schema = @Schema(type = "string")
    )
    private String name;

    @Parameter(
            description = "Filter data product versions by tag. Exact match (case-insensitive).",
            schema = @Schema(type = "string")
    )
    private String tag;

    @Parameter(
            description = "Filter data product versions by version number. Exact match (case-insensitive).",
            schema = @Schema(type = "string")
    )
    private String versionNumber;

    @Parameter(
            description = "Filter data product versions by approval state. Exact match.",
            schema = @Schema(type = "string", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    )
    private DataProductVersionValidationStateRes validationState;

    @Parameter(
            description = "Filter data product versions with matchSearch on name. Not exact match.",
            schema = @Schema(type = "string")
    )
    private String search;

    @Parameter(
            description = "Extension snapshot filter: scope id (case-sensitive). Must be used together with extensionPropertyKey and extensionPropertyValue.",
            schema = @Schema(type = "string")
    )
    private String extensionPropertyScope;

    @Parameter(
            description = "Extension snapshot filter: property key under the scope (case-sensitive). Must be used together with extensionPropertyScope and extensionPropertyValue.",
            schema = @Schema(type = "string")
    )
    private String extensionPropertyKey;

    @Parameter(
            description = "Extension snapshot filter: scalar value as text; compared to PostgreSQL jsonb_extract_path_text of the stored JSON (exact match, v1 string-oriented semantics).",
            schema = @Schema(type = "string")
    )
    private String extensionPropertyValue;

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

    public DataProductVersionValidationStateRes getValidationState() {
        return validationState;
    }

    public void setValidationState(DataProductVersionValidationStateRes validationState) {
        this.validationState = validationState;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSearch(){
        return search;
    }

    public String getExtensionPropertyScope() {
        return extensionPropertyScope;
    }

    public void setExtensionPropertyScope(String extensionPropertyScope) {
        this.extensionPropertyScope = extensionPropertyScope;
    }

    public String getExtensionPropertyKey() {
        return extensionPropertyKey;
    }

    public void setExtensionPropertyKey(String extensionPropertyKey) {
        this.extensionPropertyKey = extensionPropertyKey;
    }

    public String getExtensionPropertyValue() {
        return extensionPropertyValue;
    }

    public void setExtensionPropertyValue(String extensionPropertyValue) {
        this.extensionPropertyValue = extensionPropertyValue;
    }
}

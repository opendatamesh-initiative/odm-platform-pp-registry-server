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
            description = "Filter data product versions by approval state. Exact match.",
            schema = @Schema(type = "string", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    )
    private DataProductVersionValidationStateRes validationState;

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

    public DataProductVersionValidationStateRes getValidationState() {
        return validationState;
    }

    public void setValidationState(DataProductVersionValidationStateRes validationState) {
        this.validationState = validationState;
    }
}

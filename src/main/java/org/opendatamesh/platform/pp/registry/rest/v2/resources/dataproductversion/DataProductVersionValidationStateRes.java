package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Approval state of a data product version")
public enum DataProductVersionValidationStateRes {
    @Schema(description = "The data product version is pending approval")
    PENDING,
    
    @Schema(description = "The data product version has been approved successfully")
    APPROVED,
    
    @Schema(description = "The data product version approval has been rejected")
    REJECTED
}

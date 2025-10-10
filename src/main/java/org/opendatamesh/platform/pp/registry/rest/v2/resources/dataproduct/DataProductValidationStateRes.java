package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Approval state of a data product")
public enum DataProductValidationStateRes {
    @Schema(description = "The data product is pending approval")
    PENDING,
    
    @Schema(description = "The data product has been approved successfully")
    APPROVED,
    
    @Schema(description = "The data product approval has been rejected")
    REJECTED
}

package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation state of a data product")
public enum DataProductValidationStateRes {
    @Schema(description = "The data product is pending validation")
    PENDING,
    
    @Schema(description = "The data product has been validated successfully")
    VALIDATED,
    
    @Schema(description = "The data product validation has been rejected")
    REJECTED
}

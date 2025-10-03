package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation state of a data product version")
public enum DataProductVersionValidationStateRes {
    @Schema(description = "The data product version is pending validation")
    PENDING,
    
    @Schema(description = "The data product version has been validated successfully")
    VALIDATED,
    
    @Schema(description = "The data product version validation has been rejected")
    REJECTED
}

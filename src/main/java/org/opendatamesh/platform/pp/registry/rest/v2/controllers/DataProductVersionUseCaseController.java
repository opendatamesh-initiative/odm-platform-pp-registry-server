package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.DataProductVersionsUseCasesService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdateCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdateResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectResultRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/products-versions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Data Product Version Use Cases", description = "Endpoints for data product version use cases")
public class DataProductVersionUseCaseController {
    @Autowired
    private DataProductVersionsUseCasesService useCasesService;

    @Operation(summary = "Publish a data product version", description = "Publishes a new data product version using the specified configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Data product version published successfully",
                    content = @Content(schema = @Schema(implementation = DataProductVersionPublishResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.CREATED)
    public DataProductVersionPublishResultRes publishDataProductVersion(
            @Parameter(description = "Data product version publication command", required = true)
            @RequestBody DataProductVersionPublishCommandRes publishCommand
    ) {
        return useCasesService.publishDataProductVersion(publishCommand);
    }

    @Hidden
    @Operation(summary = "Approve a data product version", description = "Approves a data product version that is in PENDING state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product version approved successfully",
                    content = @Content(schema = @Schema(implementation = DataProductVersionApproveResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or data product version not in PENDING state"),
            @ApiResponse(responseCode = "404", description = "Data product version not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/approve")
    @ResponseStatus(HttpStatus.OK)
    public DataProductVersionApproveResultRes approveDataProductVersion(
            @Parameter(description = "Data product version approval command", required = true)
            @RequestBody DataProductVersionApproveCommandRes approveCommand
    ) {
        return useCasesService.approveDataProductVersion(approveCommand);
    }

    @Hidden
    @Operation(summary = "Reject a data product version", description = "Rejects a data product version that is in PENDING state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product version rejected successfully",
                    content = @Content(schema = @Schema(implementation = DataProductVersionRejectResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or data product version not in PENDING state"),
            @ApiResponse(responseCode = "404", description = "Data product version not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reject")
    @ResponseStatus(HttpStatus.OK)
    public DataProductVersionRejectResultRes rejectDataProductVersion(
            @Parameter(description = "Data product version rejection command", required = true)
            @RequestBody DataProductVersionRejectCommandRes rejectCommand
    ) {
        return useCasesService.rejectDataProductVersion(rejectCommand);
    }

    @Operation(summary = "Update a data product version with documentation fields", description = "Update a existing data product version using the specified documentation fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product version updated successfully",
                    content = @Content(schema = @Schema(implementation = DataProductVersionPublishResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Data product version not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/updatedocumentationfields")
    @ResponseStatus(HttpStatus.OK)
    public DataProductVersionDocumentationFieldsUpdateResultRes updateDocumentationFieldsDataProductVersion(
            @Parameter(description = "Data product documentation fields update command", required = true)
            @RequestBody DataProductVersionDocumentationFieldsUpdateCommandRes updateDocumentationFieldsCommand
    ) {
        return useCasesService.updateDocumentationFieldsDataProductVersion(updateDocumentationFieldsCommand);
    }
}

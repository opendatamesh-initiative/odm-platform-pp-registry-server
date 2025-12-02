package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductsUseCasesService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init.DataProductInitCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init.DataProductInitResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve.DataProductApproveCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve.DataProductApproveResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject.DataProductRejectCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject.DataProductRejectResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.delete.DataProductDeleteCommandRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Data Product Use Cases", description = "Endpoints for data product use cases")
public class DataProductUseCaseController {
    @Autowired
    private DataProductsUseCasesService useCasesService;

    @Operation(summary = "Initialize a data product", description = "Initializes a new data product using the specified configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Data product initialized successfully",
                    content = @Content(schema = @Schema(implementation = DataProductInitResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/init")
    @ResponseStatus(HttpStatus.CREATED)
    public DataProductInitResultRes initializeDataProduct(
            @Parameter(description = "Data product initialization command", required = true)
            @RequestBody DataProductInitCommandRes initCommand
    ) {
        return useCasesService.initializeDataProduct(initCommand);
    }

    @Hidden
    @Operation(summary = "Approve a data product", description = "Approves a data product that is in PENDING state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product approved successfully",
                    content = @Content(schema = @Schema(implementation = DataProductApproveResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or data product not in PENDING state"),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/approve")
    @ResponseStatus(HttpStatus.OK)
    public DataProductApproveResultRes approveDataProduct(
            @Parameter(description = "Data product approval command", required = true)
            @RequestBody DataProductApproveCommandRes approveCommand
    ) {
        return useCasesService.approveDataProduct(approveCommand);
    }

    @Hidden
    @Operation(summary = "Reject a data product", description = "Rejects a data product that is in PENDING state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product rejected successfully",
                    content = @Content(schema = @Schema(implementation = DataProductRejectResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or data product not in PENDING state"),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reject")
    @ResponseStatus(HttpStatus.OK)
    public DataProductRejectResultRes rejectDataProduct(
            @Parameter(description = "Data product rejection command", required = true)
            @RequestBody DataProductRejectCommandRes rejectCommand
    ) {
        return useCasesService.rejectDataProduct(rejectCommand);
    }

    @Hidden
    @Operation(summary = "Delete a data product", description = "Deletes a data product and all its associated versions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Data product deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDataProduct(
            @Parameter(description = "Data product deletion command", required = true)
            @RequestBody DataProductDeleteCommandRes deleteCommand
    ) {
        useCasesService.deleteDataProduct(deleteCommand);
    }
}

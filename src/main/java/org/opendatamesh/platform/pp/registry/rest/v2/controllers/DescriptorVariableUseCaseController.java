package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.DescriptorVariableUseCasesService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.usecases.store.StoreDescriptorVariableResultRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/descriptor-variables", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Descriptor Variables", description = "Endpoints for managing descriptor variables")
public class DescriptorVariableUseCaseController {

    private final DescriptorVariableUseCasesService descriptorVariableUseCasesService;

    @Autowired
    public DescriptorVariableUseCaseController(DescriptorVariableUseCasesService descriptorVariableUseCasesService) {
        this.descriptorVariableUseCasesService = descriptorVariableUseCasesService;
    }

    @Operation(summary = "Store a descriptor variable", description = "Stores a descriptor variable using the use case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Descriptor variable stored successfully",
                    content = @Content(schema = @Schema(implementation = StoreDescriptorVariableResultRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/store")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreDescriptorVariableResultRes storeDescriptorVariable(
            @Parameter(description = "Store descriptor variable command", required = true)
            @RequestBody StoreDescriptorVariableCommandRes storeCommand
    ) {
        return descriptorVariableUseCasesService.storeDescriptorVariable(storeCommand);
    }
}

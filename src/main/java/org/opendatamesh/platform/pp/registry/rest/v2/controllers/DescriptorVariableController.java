package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.descriptorvariable.services.core.DescriptorVariableCrudService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableSearchOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/descriptor-variables", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Descriptor Variables", description = "Endpoints for managing descriptor variables")
public class DescriptorVariableController {

    private final DescriptorVariableCrudService descriptorVariableCrudService;

    @Autowired
    public DescriptorVariableController(DescriptorVariableCrudService descriptorVariableCrudService) {
        this.descriptorVariableCrudService = descriptorVariableCrudService;
    }

    @Operation(summary = "Create a new descriptor variable", description = "Creates a new descriptor variable in the registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Descriptor variable created successfully",
                    content = @Content(schema = @Schema(implementation = DescriptorVariableRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Hidden
    public DescriptorVariableRes createDescriptorVariable(
            @Parameter(description = "Descriptor variable details", required = true)
            @RequestBody DescriptorVariableRes descriptorVariable
    ) {
        return descriptorVariableCrudService.createResource(descriptorVariable);
    }

    @Operation(summary = "Get descriptor variable by sequence ID", description = "Retrieves a descriptor variable by its sequence ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descriptor variable found",
                    content = @Content(schema = @Schema(implementation = DescriptorVariableRes.class))),
            @ApiResponse(responseCode = "404", description = "Descriptor variable not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{sequenceId}")
    @ResponseStatus(HttpStatus.OK)
    public DescriptorVariableRes getDescriptorVariable(
            @Parameter(description = "Descriptor variable sequence ID", required = true)
            @PathVariable("sequenceId") Long sequenceId
    ) {
        return descriptorVariableCrudService.findOneResource(sequenceId);
    }

    @Operation(summary = "Search descriptor variables", description = "Retrieves a paginated list of descriptor variables based on search criteria. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descriptor variables found",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters or invalid sort property"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<DescriptorVariableRes> searchDescriptorVariables(
            @Parameter(description = "Search options for filtering descriptor variables")
            DescriptorVariableSearchOptions searchOptions,
            @PageableDefault(page = 0, size = 20)
            Pageable pageable
    ) {
        return descriptorVariableCrudService.findAllResourcesFiltered(pageable, searchOptions);
    }

    @Operation(summary = "Update descriptor variable by sequence ID", description = "Updates an existing descriptor variable by its sequence ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descriptor variable updated successfully",
                    content = @Content(schema = @Schema(implementation = DescriptorVariableRes.class))),
            @ApiResponse(responseCode = "404", description = "Descriptor variable not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{sequenceId}")
    @ResponseStatus(HttpStatus.OK)
    @Hidden
    public DescriptorVariableRes updateDescriptorVariable(
            @Parameter(description = "Descriptor variable sequence ID", required = true)
            @PathVariable("sequenceId") Long sequenceId,
            @Parameter(description = "Updated descriptor variable details", required = true)
            @RequestBody DescriptorVariableRes descriptorVariable
    ) {
        return descriptorVariableCrudService.overwriteResource(sequenceId, descriptorVariable);
    }

    @Operation(summary = "Delete descriptor variable by sequence ID", description = "Deletes a descriptor variable by its sequence ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Descriptor variable deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Descriptor variable not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{sequenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDescriptorVariable(
            @Parameter(description = "Descriptor variable sequence ID", required = true)
            @PathVariable("sequenceId") Long sequenceId
    ) {
        descriptorVariableCrudService.delete(sequenceId);
    }
}

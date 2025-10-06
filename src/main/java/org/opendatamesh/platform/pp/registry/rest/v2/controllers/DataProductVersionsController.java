package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionsQueryService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionShortRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/products-versions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Data Product Versions", description = "Endpoints for managing data product versions")
public class DataProductVersionsController {

    /**
     * Service for CRUD operations on individual DataProductVersion entities.
     * Used for create, read (single), update, and delete operations.
     */
    @Autowired
    private DataProductVersionCrudService dataProductVersionCrudService;

    /**
     * Service for querying multiple DataProductVersion entities.
     * Used for paginated search and listing operations with better performance.
     */
    @Autowired
    private DataProductVersionsQueryService dataProductVersionsQueryService;

    @Hidden
    @Operation(summary = "Create a new data product version", description = "Creates a new data product version in the registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Data product version created successfully",
                    content = @Content(schema = @Schema(implementation = DataProductVersionRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataProductVersionRes createDataProductVersion(
            @Parameter(description = "Data product version details", required = true)
            @RequestBody DataProductVersionRes dataProductVersion
    ) {
        return dataProductVersionCrudService.createResource(dataProductVersion);
    }

    @Operation(summary = "Get data product version by UUID", description = "Retrieves a data product version by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product version found",
                    content = @Content(schema = @Schema(implementation = DataProductVersionRes.class))),
            @ApiResponse(responseCode = "404", description = "Data product version not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public DataProductVersionRes getDataProductVersion(
            @Parameter(description = "Data product version UUID", required = true)
            @PathVariable("uuid") String uuid
    ) {
        return dataProductVersionCrudService.findOneResource(uuid);
    }

    @Operation(summary = "Search data product versions", description = "Retrieves a paginated list of data product versions based on search criteria. " +
            "The results can be sorted by any of the following properties: uuid, dataProductUuid, name, description, tag, validationState, createdAt, updatedAt. " +
            "Sort direction can be specified using 'asc' or 'desc' (e.g., 'sort=name,desc'). Returns short resources for better performance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product versions found",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters or invalid sort property"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<DataProductVersionShortRes> searchDataProductVersions(
            @Parameter(description = "Search options for filtering data product versions")
            DataProductVersionSearchOptions searchOptions,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by createdAt in descending order. " +
                    "Valid sort properties are: uuid, dataProductUuid, name, description, tag, validationState, createdAt, updatedAt")
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return dataProductVersionsQueryService.findAllResourcesShort(pageable, searchOptions);
    }

    @Hidden
    @Operation(summary = "Update data product version by UUID", description = "Updates an existing data product version by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product version updated successfully",
                    content = @Content(schema = @Schema(implementation = DataProductVersionRes.class))),
            @ApiResponse(responseCode = "404", description = "Data product version not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public DataProductVersionRes updateDataProductVersion(
            @Parameter(description = "Data product version UUID", required = true)
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Updated data product version details", required = true)
            @RequestBody DataProductVersionRes dataProductVersion
    ) {
        return dataProductVersionCrudService.overwriteResource(uuid, dataProductVersion);
    }

    @Hidden
    @Operation(summary = "Delete data product version by UUID", description = "Deletes a data product version by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Data product version deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Data product version not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDataProductVersion(
            @Parameter(description = "Data product version UUID", required = true)
            @PathVariable("uuid") String uuid
    ) {
        dataProductVersionCrudService.delete(uuid);
    }

}

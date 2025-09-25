package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Data Products", description = "Endpoints for managing data products")
public class DataProductController {

    @Autowired
    private DataProductsService dataProductsService;

    @Operation(summary = "Create a new data product", description = "Creates a new data product in the registry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Data product created successfully",
                    content = @Content(schema = @Schema(implementation = DataProductRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataProductRes createDataProduct(
            @Parameter(description = "Data product details", required = true)
            @RequestBody DataProductRes dataProduct
    ) {
        return dataProductsService.createResource(dataProduct);
    }

    @Operation(summary = "Get data product by ID", description = "Retrieves a specific data product by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product found",
                    content = @Content(schema = @Schema(implementation = DataProductRes.class))),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DataProductRes getDataProduct(
            @Parameter(description = "Data product UUID", required = true)
            @PathVariable("id") String id
    ) {
        return dataProductsService.findOneResource(id);
    }

    @Operation(summary = "Search data products", description = "Retrieves a paginated list of data products based on search criteria. " +
            "The results can be sorted by any of the following properties: uuid, fqn, domain, name, displayName, description, " +
            "version, createdAt, updatedAt. Sort direction can be specified using 'asc' or 'desc' (e.g., 'sort=name,desc').")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data products found",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters or invalid sort property"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<DataProductRes> searchDataProducts(
            @Parameter(description = "Search options for filtering data products")
            DataProductSearchOptions searchOptions,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by createdAt in descending order. " +
                    "Valid sort properties are: uuid, fqn, domain, name, displayName, description, version, createdAt, updatedAt")
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return dataProductsService.findAllResourcesFiltered(pageable, searchOptions);
    }

    @Operation(summary = "Update data product", description = "Updates an existing data product by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data product updated successfully",
                    content = @Content(schema = @Schema(implementation = DataProductRes.class))),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DataProductRes updateDataProduct(
            @Parameter(description = "Data product UUID", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Updated data product details", required = true)
            @RequestBody DataProductRes dataProduct
    ) {
        return dataProductsService.overwriteResource(id, dataProduct);
    }

    @Operation(summary = "Delete data product", description = "Deletes a data product by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Data product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDataProduct(
            @Parameter(description = "Data product UUID", required = true)
            @PathVariable("id") String id
    ) {
        dataProductsService.delete(id);
    }
}

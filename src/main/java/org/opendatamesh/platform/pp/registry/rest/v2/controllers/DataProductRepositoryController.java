package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductRepositoryUtilsService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.TagRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/products/{uuid}/repository", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Data Product Repository", description = "Endpoints for managing data product repository")
public class DataProductRepositoryController {

    private final DataProductRepositoryUtilsService dataProductRepositoryUtilsService;

    public DataProductRepositoryController(DataProductRepositoryUtilsService dataProductRepositoryUtilsService) {
        this.dataProductRepositoryUtilsService = dataProductRepositoryUtilsService;
    }

    @Operation(summary = "Get repository commits", description = "Retrieves a paginated list of commits from the data product's repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commits retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "400", description = "Data product does not have an associated repository"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/commits")
    @ResponseStatus(HttpStatus.OK)
    public Page<CommitRes> getRepositoryCommits(
            @Parameter(description = "Data product UUID", required = true)
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Search options for filtering commits by tag names, branch name or commit hashes")
            CommitSearchOptions searchOptions,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(page = 0, size = 20, sort = "authorDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "HTTP headers for Git provider authentication")
            @RequestHeader HttpHeaders headers
    ) {
        return dataProductRepositoryUtilsService.listCommits(uuid, headers, searchOptions, pageable);
    }

    @Operation(summary = "Get repository branches", description = "Retrieves a paginated list of branches from the data product's repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Branches retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "400", description = "Data product does not have an associated repository"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/branches")
    @ResponseStatus(HttpStatus.OK)
    public Page<BranchRes> getRepositoryBranches(
            @Parameter(description = "Data product UUID", required = true)
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
            @Parameter(description = "HTTP headers for Git provider authentication")
            @RequestHeader HttpHeaders headers
    ) {
        return dataProductRepositoryUtilsService.listBranches(uuid, headers, pageable);
    }

    @Operation(summary = "Get repository tags", description = "Retrieves a paginated list of tags from the data product's repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Data product not found"),
            @ApiResponse(responseCode = "400", description = "Data product does not have an associated repository"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/tags")
    @ResponseStatus(HttpStatus.OK)
    public Page<TagRes> getRepositoryTags(
            @Parameter(description = "Data product UUID", required = true)
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(page = 0, size = 20, sort = "tagDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "HTTP headers for Git provider authentication")
            @RequestHeader HttpHeaders headers
    ) {
        return dataProductRepositoryUtilsService.listTags(uuid, headers, pageable);
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public TagRes createTag(
            @Parameter(description = "Data product UUID", required = true) @PathVariable("uuid") String uuid,
            @Parameter(description = "HTTP headers for Git provider authentication") @RequestHeader HttpHeaders headers,
            @Parameter(description = "Tag details", required = true) @RequestBody TagRes tagRes) {
        return dataProductRepositoryUtilsService.addTag(uuid, tagRes, headers);
    }
}

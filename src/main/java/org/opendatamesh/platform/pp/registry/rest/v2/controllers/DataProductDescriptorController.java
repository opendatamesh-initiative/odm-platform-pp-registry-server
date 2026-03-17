package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductsDescriptorService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.GetDescriptorOptionsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.InitDescriptorCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.UpdateDescriptorCommandRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataProductDescriptorController {

    @Autowired
    private DataProductsDescriptorService dataProductsDescriptorService;

    @GetMapping("/{uuid}/descriptor")
    @Operation(
            summary = "Gets the descriptor file associated with a data product",
            description = """
                    Gets the descriptor file associated with a data product.
                    
                    This endpoint requires authentication headers because it internally 
                    accesses the Git provider (GitHub, GitLab, Bitbucket, Azure DevOps) 
                    to fetch the data product descriptor.
                    """
    )
    public JsonNode getDescriptor(
            @PathVariable @Parameter(description = "The Data Product resource identifier", required = true) String uuid,
            @Parameter(description = "Options for tag, branch, commit (query params)")
            GetDescriptorOptionsRes options,
            @Parameter(description = "HTTP headers for Git provider authentication")
            @RequestHeader HttpHeaders headers) {
        return dataProductsDescriptorService.getDescriptor(uuid, options, headers);
    }

    @PostMapping("/{uuid}/descriptor")
    @Operation(
            summary = "Initializes the descriptor file associated with a data product",
            description = """
                    Initializes (writes) the descriptor file in the underlying Git repository for a data product.
                    
                    This endpoint requires authentication headers because it internally
                    pushes to the Git provider (GitHub, GitLab, Bitbucket, Azure DevOps).
                    """
    )
    @ResponseStatus(HttpStatus.OK)
    public void initDescriptor(
            @PathVariable @Parameter(description = "The Data Product resource identifier", required = true) String uuid,
            @Parameter(description = "The descriptor file content (JSON/YAML)")
            @RequestBody JsonNode content,
            @Parameter(description = "Query params: branch, authorName, authorEmail")
            InitDescriptorCommandRes options,
            @Parameter(description = "HTTP headers for Git provider authentication")
            @RequestHeader HttpHeaders headers) {
        dataProductsDescriptorService.initDescriptor(uuid, content, options, headers);
    }

    @PutMapping("/{uuid}/descriptor")
    @Operation(
            summary = "Updates the descriptor file associated with a data product",
            description = """
                    Updates (writes) the descriptor file in the underlying Git repository for a data product.
                    
                    This endpoint requires authentication headers because it internally
                    pushes to the Git provider (GitHub, GitLab, Bitbucket, Azure DevOps).
                    """
    )
    @ResponseStatus(HttpStatus.OK)
    public void modifyDescriptor(
            @PathVariable @Parameter(description = "The Data Product resource identifier", required = true) String uuid,
            @Parameter(description = "The new descriptor file content (JSON/YAML)")
            @RequestBody JsonNode content,
            @Parameter(description = "Query params: branch, commitMessage, baseCommit, authorName, authorEmail")
            UpdateDescriptorCommandRes options,
            @Parameter(description = "HTTP headers for Git provider authentication")
            @RequestHeader HttpHeaders headers) {
        dataProductsDescriptorService.updateDescriptor(uuid, content, options, headers);
    }
}


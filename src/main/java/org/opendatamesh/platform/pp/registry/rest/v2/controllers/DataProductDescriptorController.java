package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductsDescriptorService;
import org.opendatamesh.platform.pp.registry.dataproduct.services.GitReference;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.CredentialFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataProductDescriptorController {

    @Autowired
    private DataProductsService dataProductsService;

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

        **Expected headers for authentication:**
        - `x-odm-gpauth-type`: The type of credential. Currently supported: "PAT".
        - `x-odm-gpauth-param-username`: Optional username for PAT credentials.
        - `x-odm-gpauth-param-token`: The personal access token for PAT credentials.
        """
    )
    public Optional<JsonNode> getDescriptor(
            @Parameter(description = "The Data Product resource identifier")
            @PathVariable(value = "uuid") String uuid,
            @Parameter(description = "Optional tag to select a specific version")
            @RequestParam(value = "tag", required = false) String tag,
            @Parameter(description = "Optional branch name")
            @RequestParam(value = "branch", required = false) String branch,
            @Parameter(description = "Optional commit SHA")
            @RequestParam(value = "commit", required = false) String commit,
            @Parameter(description = "HTTP headers containing credentials")
            @RequestHeader HttpHeaders headers) {

        GitReference referencePointer = new GitReference(tag, branch, commit);

        // Extract credentials from headers; required to access the Git provider
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        return dataProductsDescriptorService.getDescriptor(uuid, referencePointer, credential);
    }
}


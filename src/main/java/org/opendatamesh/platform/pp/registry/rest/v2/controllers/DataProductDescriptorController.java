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

    /**
     * Gets the descriptor file associated with a data product.
     *
     * <p>This endpoint requires authentication headers because it internally accesses
     * the Git provider (GitHub, GitLab, Bitbucket, Azure DevOps) to fetch the data product descriptor.</p>
     *
     * <p>Expected headers for authentication:</p>
     * <ul>
     *   <li><b>x-odm-gpauth-type</b>: The type of credential. Currently supported: "PAT".</li>
     *   <li><b>x-odm-gpauth-param-username</b>: Optional username for PAT credentials.</li>
     *   <li><b>x-odm-gpauth-param-token</b>: The personal access token for PAT credentials.</li>
     * </ul>
     * </p>
     *
     * @param uuid The Data Product resource identifier
     * @param tag Optional tag to select a specific version
     * @param branch Optional branch name
     * @param commit Optional commit SHA
     * @param headers HTTP headers containing credentials
     * @return The descriptor file as JSON
     */
    @GetMapping("/{uuid}/descriptor")
    @Operation(summary = "Gets the descriptor file associated with a data product")
    public Optional<JsonNode> getDescriptor(
            @Parameter(description = "The Data Product resource identifier")
            @PathVariable(value = "uuid") String uuid,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "commit", required = false) String commit,
            @RequestHeader HttpHeaders headers) {

        GitReference referencePointer = new GitReference(tag, branch, commit);

        // Extract credentials from headers; required to access the Git provider
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        return dataProductsDescriptorService.getDescriptor(uuid, referencePointer, credential);
    }
}


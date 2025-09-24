package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductDescriptorService;
import org.opendatamesh.platform.pp.registry.dataproduct.services.VersionPointer;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductService;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.CredentialFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@Hidden
@RequestMapping(value = "/api/v1/dataproducts", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataProductDescriptorController {

    @Autowired
    private DataProductService dataProductService;

    @Autowired
    private DataProductDescriptorService dataProductDescriptorService;

    @GetMapping("/{uuid}/descriptor")
    @Hidden
    @Operation(summary = "Gets the descriptor file associated with a data product")
    public Optional<JsonNode> getDescriptor(
            @Parameter(description = "The Data Product resource identifier")
            @PathVariable(value = "uuid") String uuid,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "commit", required = false) String commit,
            @RequestHeader HttpHeaders headers) {

        VersionPointer pointer = new VersionPointer(tag, branch, commit);
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap());
        return dataProductDescriptorService.getDescriptor(uuid, pointer, credential);
    }

}

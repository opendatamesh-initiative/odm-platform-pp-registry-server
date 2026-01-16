package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/pp/registry", produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistryV1Controller {

    @Autowired
    private RegistryV1Service registryV1Service;

    @GetMapping(value = "/products/{id}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    public String getDataProductVersionEndpoint(
            @PathVariable(value = "id") String id,
            @PathVariable(value = "version") String version,
            @RequestParam(name = "format", required = false) String format
    ) {
        return registryV1Service.getDataProductVersion(id, version, format);
    }

    @GetMapping(value = "/templates/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RegistryV1ExternalComponentResource getTemplateEndpoint(
            @Parameter(description = "Identifier of the Template definition") @PathVariable(value = "id") String id) {

        return registryV1Service.getTemplate(id);
    }


    @GetMapping(value = "/products/{id}/versions/{version}/variables")
    @ResponseStatus(HttpStatus.OK)
    public List<RegistryV1VariableResource> getVariablesEndpoint(

            @PathVariable(value = "id") String id,
            @PathVariable(value = "version") String version
    ) {
        return registryV1Service.getVariables(id, version);
    }

    @PutMapping(value = "/products/{id}/versions/{version}/variables/{varId}")
    @ResponseStatus(HttpStatus.OK)
    public RegistryV1VariableResource updateVariableEndpoint(
            @PathVariable(value = "id") String id,
            @PathVariable(value = "version") String version,
            @PathVariable(value = "varId") Long variableId,
            @RequestParam(name = "value") String variableValue
    ) {
        return registryV1Service.updateVariable(id, version, variableId, variableValue);
    }

}

package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "providerCustomResourcesDefinitionsRes", description = "Custom resource definitions required for a resource type with a specific provider")
public class ProviderCustomResourcesDefinitionsRes {
    
    @Schema(description = "List of custom resource definitions")
    private List<ProviderCustomResourceDefinitionRes> definitions;

    public ProviderCustomResourcesDefinitionsRes() {
    }

    public ProviderCustomResourcesDefinitionsRes(List<ProviderCustomResourceDefinitionRes> additionalProviderProperties) {
        this.definitions = additionalProviderProperties;
    }

    public List<ProviderCustomResourceDefinitionRes> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<ProviderCustomResourceDefinitionRes> definitions) {
        this.definitions = definitions;
    }
}


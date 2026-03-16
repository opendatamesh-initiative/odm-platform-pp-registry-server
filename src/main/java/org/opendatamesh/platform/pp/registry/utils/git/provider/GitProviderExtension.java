package org.opendatamesh.platform.pp.registry.utils.git.provider;

import java.util.List;

import org.opendatamesh.platform.pp.registry.utils.git.model.ProviderCustomResourceDefinition;

public interface GitProviderExtension {
    /**
     * Get custom definition for a specific resource type supported by this provider.
     * Each Git provider may require provider-specific properties when creating or managing resources
     * (e.g., workspace for Bitbucket repositories, visibility settings for GitHub repositories).
     *
     * @param modelResourceType the resource type to get property definitions for
     * @return list of resource definitions, each containing name, type, and required flag.
     * Returns an empty list if no additional properties are defined for the given resource type
     */
    List<ProviderCustomResourceDefinition> getProviderCustomResourceDefinitions(GitProviderModelResourceType modelResourceType);

}

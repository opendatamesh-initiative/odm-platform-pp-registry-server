package org.opendatamesh.platform.pp.registry.utils.git.provider;

import org.opendatamesh.platform.pp.registry.utils.git.model.ProviderCustomResourceDefinition;

import java.util.List;

public interface GitProviderModelExtension {
    boolean support(GitProviderModelResourceType resourceType);

    List<ProviderCustomResourceDefinition> getCustomResourcesDefinitions();
}

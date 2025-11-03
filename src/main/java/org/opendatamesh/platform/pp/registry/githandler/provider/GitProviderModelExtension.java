package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.githandler.model.ProviderCustomResourceDefinition;

import java.util.List;

public interface GitProviderModelExtension {
    boolean support(GitProviderModelResourceType resourceType);

    List<ProviderCustomResourceDefinition> getCustomResourcesDefinitions();
}

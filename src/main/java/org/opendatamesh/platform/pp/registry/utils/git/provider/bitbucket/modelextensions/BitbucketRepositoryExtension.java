package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.modelextensions;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.opendatamesh.platform.pp.registry.utils.git.model.ProviderCustomResourceDefinition;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderModelExtension;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderModelResourceType;

import java.util.List;

public class BitbucketRepositoryExtension implements GitProviderModelExtension {

    public static final String PROJECT = "project";
    public static final String ORGANIZATION = "organization"; //aka workspace for bitbucket

    @Override
    public boolean support(GitProviderModelResourceType o) {
        return GitProviderModelResourceType.REPOSITORY.equals(o);
    }

    @Override
    public List<ProviderCustomResourceDefinition> getCustomResourcesDefinitions() {
        return List.of(
                new ProviderCustomResourceDefinition(
                        PROJECT, JsonNodeType.OBJECT.name(), true
                )
        );
    }
}

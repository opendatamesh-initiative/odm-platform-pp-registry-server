package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getrepository;

import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryOwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryVisibility;

public abstract class AzureGetRepositoryMapper {

    public static Repository toInternalModel(AzureGetRepositoryRepositoryRes repoRes, String projectId) {
        if (repoRes == null) {
            return null;
        }

        return new Repository(
                repoRes.getId(),
                repoRes.getName(),
                repoRes.getDescription(),
                repoRes.getRemoteUrl(),
                null,
                repoRes.getDefaultBranch(),
                RepositoryOwnerType.ORGANIZATION,
                        projectId,
                RepositoryVisibility.PRIVATE
        );
    }
}


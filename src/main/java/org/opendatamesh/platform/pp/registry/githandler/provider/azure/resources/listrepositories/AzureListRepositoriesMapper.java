package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories;

import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryOwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryVisibility;

public abstract class AzureListRepositoriesMapper {

    public static Repository toInternalModel(AzureListRepositoriesRepositoryRes repoRes, String projectId) {
        if (repoRes == null) {
            return null;
        }

        return new Repository(
                repoRes.getId(),
                repoRes.getName(),
                repoRes.getDescription(),
                repoRes.getRemoteUrl(),
                null, // SSH URL not always available
                repoRes.getDefaultBranch(),
                RepositoryOwnerType.ORGANIZATION,
                        projectId,
                RepositoryVisibility.PRIVATE // Azure DevOps repos are typically private
        );
    }
}


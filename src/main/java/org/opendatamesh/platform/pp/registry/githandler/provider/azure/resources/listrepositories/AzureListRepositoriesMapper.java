package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listrepositories;

import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;

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
                OwnerType.ORGANIZATION,
                projectId,
                Visibility.PRIVATE // Azure DevOps repos are typically private
        );
    }
}


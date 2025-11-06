package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.createrepository;

import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;

public abstract class AzureCreateRepositoryMapper {

    public static Repository toInternalModel(AzureCreateRepositoryRepositoryRes repoRes, String projectId) {
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
                OwnerType.ORGANIZATION,
                projectId,
                Visibility.PRIVATE // Azure DevOps repos are always private
        );
    }

    public static AzureCreateRepositoryReq fromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }

        AzureCreateRepositoryReq request = new AzureCreateRepositoryReq();
        request.setName(repository.getName());

        AzureCreateRepositoryProjectReferenceReq projectRef = new AzureCreateRepositoryProjectReferenceReq();
        projectRef.setId(repository.getOwnerId());
        request.setProject(projectRef);

        return request;
    }
}


package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getrepository;

import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;

public abstract class GitLabGetRepositoryMapper {

    public static Repository toInternalModel(GitLabGetRepositoryProjectRes projectRes) {
        if (projectRes == null) {
            return null;
        }

        String ownerId = projectRes.getCreatorId() != null ? String.valueOf(projectRes.getCreatorId()) :
                (projectRes.getNamespace() != null ? String.valueOf(projectRes.getNamespace().getId()) : null);

        return new Repository(
                String.valueOf(projectRes.getId()),
                projectRes.getName(),
                projectRes.getDescription(),
                projectRes.getHttpUrlToRepo(),
                projectRes.getSshUrlToRepo(),
                projectRes.getDefaultBranch(),
                OwnerType.ACCOUNT, // Default to ACCOUNT
                ownerId,
                projectRes.getVisibility().equals("private") ? Visibility.PRIVATE : Visibility.PUBLIC
        );
    }
}


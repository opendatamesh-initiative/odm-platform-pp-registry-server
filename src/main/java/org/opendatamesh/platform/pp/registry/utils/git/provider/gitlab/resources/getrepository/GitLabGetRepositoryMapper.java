package org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getrepository;

import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryOwnerType;
import org.opendatamesh.platform.pp.registry.utils.git.model.Repository;
import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryVisibility;

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
                RepositoryOwnerType.ACCOUNT, // Default to ACCOUNT
                ownerId,
                projectRes.getVisibility().equals("private") ? RepositoryVisibility.PRIVATE
                        : RepositoryVisibility.PUBLIC
        );
    }
}


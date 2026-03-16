package org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.createrepository;

import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryOwnerType;
import org.opendatamesh.platform.pp.registry.utils.git.model.Repository;
import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryVisibility;

public abstract class GitLabCreateRepositoryMapper {

    public static Repository toInternalModel(GitLabCreateRepositoryProjectRes projectRes,
            RepositoryOwnerType ownerType) {
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
                ownerType,
                ownerId,
                projectRes.getVisibility().equals("private") ? RepositoryVisibility.PRIVATE
                        : RepositoryVisibility.PUBLIC
        );
    }

    public static GitLabCreateRepositoryReq fromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }

        GitLabCreateRepositoryReq request = new GitLabCreateRepositoryReq();
        request.setName(repository.getName());
        request.setDescription(repository.getDescription());
        request.setVisibility(repository.getVisibility() == RepositoryVisibility.PRIVATE ? "private" : "public");

        // Set namespace_id based on owner type
        if (repository.getOwnerType() == RepositoryOwnerType.ORGANIZATION) {
            request.setNamespaceId(repository.getOwnerId());
        } else {
            request.setNamespaceId(null);
        }

        return request;
    }
}


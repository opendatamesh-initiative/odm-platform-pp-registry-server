package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.createrepository;

import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;

public abstract class GitLabCreateRepositoryMapper {

    public static Repository toInternalModel(GitLabCreateRepositoryProjectRes projectRes, OwnerType ownerType) {
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
                projectRes.getVisibility().equals("private") ? Visibility.PRIVATE : Visibility.PUBLIC
        );
    }

    public static GitLabCreateRepositoryReq fromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }

        GitLabCreateRepositoryReq request = new GitLabCreateRepositoryReq();
        request.setName(repository.getName());
        request.setDescription(repository.getDescription());
        request.setVisibility(repository.getVisibility() == Visibility.PRIVATE ? "private" : "public");

        // Set namespace_id based on owner type
        if (repository.getOwnerType() == OwnerType.ORGANIZATION) {
            request.setNamespaceId(repository.getOwnerId());
        } else {
            request.setNamespaceId(null);
        }

        return request;
    }
}


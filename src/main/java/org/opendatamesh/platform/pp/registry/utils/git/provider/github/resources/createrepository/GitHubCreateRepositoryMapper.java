package org.opendatamesh.platform.pp.registry.utils.git.provider.github.resources.createrepository;

import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryOwnerType;
import org.opendatamesh.platform.pp.registry.utils.git.model.Repository;
import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryVisibility;

public abstract class GitHubCreateRepositoryMapper {

    private static RepositoryOwnerType determineOwnerType(GitHubCreateRepositoryUserRes owner) {
        if (owner != null && "Organization".equals(owner.getType())) {
            return RepositoryOwnerType.ORGANIZATION;
        }
        return RepositoryOwnerType.ACCOUNT;
    }

    public static Repository toInternalModel(GitHubCreateRepositoryRepositoryRes repoRes) {
        if (repoRes == null) {
            return null;
        }

        return new Repository(
                String.valueOf(repoRes.getId()),
                repoRes.getName(),
                repoRes.getDescription(),
                repoRes.getCloneUrl(),
                repoRes.getSshUrl(),
                repoRes.getDefaultBranch(),
                determineOwnerType(repoRes.getOwner()),
                String.valueOf(repoRes.getOwner().getId()),
                repoRes.isPrivate() ? RepositoryVisibility.PRIVATE : RepositoryVisibility.PUBLIC
        );
    }

    public static GitHubCreateRepositoryReq fromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }

        GitHubCreateRepositoryReq request = new GitHubCreateRepositoryReq();
        request.setName(repository.getName());
        request.setDescription(repository.getDescription());
        request.setPrivate(repository.getVisibility() == RepositoryVisibility.PRIVATE);

        return request;
    }
}


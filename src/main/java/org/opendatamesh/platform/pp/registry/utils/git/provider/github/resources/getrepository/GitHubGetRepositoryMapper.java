package org.opendatamesh.platform.pp.registry.utils.git.provider.github.resources.getrepository;

import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryOwnerType;
import org.opendatamesh.platform.pp.registry.utils.git.model.Repository;
import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryVisibility;

public abstract class GitHubGetRepositoryMapper {

    private static RepositoryOwnerType determineOwnerType(GitHubGetRepositoryUserRes owner) {
        if (owner != null && "Organization".equals(owner.getType())) {
            return RepositoryOwnerType.ORGANIZATION;
        }
        return RepositoryOwnerType.ACCOUNT;
    }

    public static Repository toInternalModel(GitHubGetRepositoryRepositoryRes repoRes) {
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
}


package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listrepositories;

import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryOwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryVisibility;

public abstract class GitHubListRepositoriesMapper {

    private static RepositoryOwnerType determineOwnerType(GitHubListRepositoriesUserRes owner) {
        if (owner != null && "Organization".equals(owner.getType())) {
            return RepositoryOwnerType.ORGANIZATION;
        }
        return RepositoryOwnerType.ACCOUNT;
    }

    public static Repository toInternalModel(GitHubListRepositoriesRepositoryRes repoRes) {
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


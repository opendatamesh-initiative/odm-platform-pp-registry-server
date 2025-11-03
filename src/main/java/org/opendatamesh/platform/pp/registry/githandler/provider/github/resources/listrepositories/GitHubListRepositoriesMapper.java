package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listrepositories;

import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;

public abstract class GitHubListRepositoriesMapper {

    private static OwnerType determineOwnerType(GitHubListRepositoriesUserRes owner) {
        if (owner != null && "Organization".equals(owner.getType())) {
            return OwnerType.ORGANIZATION;
        }
        return OwnerType.ACCOUNT;
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
                repoRes.isPrivate() ? Visibility.PRIVATE : Visibility.PUBLIC
        );
    }
}


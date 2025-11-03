package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.createrepository;

import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;

public abstract class GitHubCreateRepositoryMapper {

    private static OwnerType determineOwnerType(GitHubCreateRepositoryUserRes owner) {
        if (owner != null && "Organization".equals(owner.getType())) {
            return OwnerType.ORGANIZATION;
        }
        return OwnerType.ACCOUNT;
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
                repoRes.isPrivate() ? Visibility.PRIVATE : Visibility.PUBLIC
        );
    }

    public static GitHubCreateRepositoryReq fromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }

        GitHubCreateRepositoryReq request = new GitHubCreateRepositoryReq();
        request.setName(repository.getName());
        request.setDescription(repository.getDescription());
        request.setPrivate(repository.getVisibility() == Visibility.PRIVATE);

        return request;
    }
}


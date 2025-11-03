package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.getcurrentuser;

import org.opendatamesh.platform.pp.registry.githandler.model.User;

public abstract class GitHubGetCurrentUserMapper {

    public static User toInternalModel(GitHubGetCurrentUserUserRes userRes) {
        if (userRes == null) {
            return null;
        }

        return new User(
                String.valueOf(userRes.getId()),
                userRes.getLogin(),
                userRes.getName() != null ? userRes.getName() : userRes.getLogin(),
                userRes.getAvatarUrl(),
                userRes.getHtmlUrl()
        );
    }
}


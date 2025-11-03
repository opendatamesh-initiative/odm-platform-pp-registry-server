package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getcurrentuser;

import org.opendatamesh.platform.pp.registry.githandler.model.User;

public abstract class GitLabGetCurrentUserMapper {

    public static User toInternalModel(GitLabGetCurrentUserUserRes userRes) {
        if (userRes == null) {
            return null;
        }

        return new User(
                String.valueOf(userRes.getId()),
                userRes.getUsername(),
                userRes.getName(),
                userRes.getAvatarUrl(),
                userRes.getWebUrl()
        );
    }
}


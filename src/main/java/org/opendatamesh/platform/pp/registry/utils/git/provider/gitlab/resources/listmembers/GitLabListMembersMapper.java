package org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.listmembers;

import org.opendatamesh.platform.pp.registry.utils.git.model.User;

public abstract class GitLabListMembersMapper {

    public static User toInternalModel(GitLabListMembersUserRes userRes) {
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


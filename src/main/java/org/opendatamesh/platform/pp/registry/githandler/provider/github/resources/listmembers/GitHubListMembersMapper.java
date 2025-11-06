package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listmembers;

import org.opendatamesh.platform.pp.registry.githandler.model.User;

public abstract class GitHubListMembersMapper {

    public static User toInternalModel(GitHubListMembersUserRes userRes) {
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


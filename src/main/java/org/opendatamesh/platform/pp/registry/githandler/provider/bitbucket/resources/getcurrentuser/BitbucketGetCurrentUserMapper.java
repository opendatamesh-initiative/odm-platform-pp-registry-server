package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getcurrentuser;

import org.opendatamesh.platform.pp.registry.githandler.model.User;

public abstract class BitbucketGetCurrentUserMapper {

    /**
     * Maps BitbucketGetCurrentUserUserRes to internal User model
     */
    public static User toInternalModel(BitbucketGetCurrentUserUserRes userRes) {
        if (userRes == null) {
            return null;
        }

        String avatarUrl = null;
        String htmlUrl = null;

        if (userRes.getLinks() != null) {
            if (userRes.getLinks().getAvatar() != null) {
                avatarUrl = userRes.getLinks().getAvatar().getHref();
            }
            if (userRes.getLinks().getHtml() != null) {
                htmlUrl = userRes.getLinks().getHtml().getHref();
            }
        }

        return new User(userRes.getUuid(), userRes.getUsername(), userRes.getDisplayName(), avatarUrl, htmlUrl);
    }

    /**
     * Creates BitbucketGetCurrentUserUserRes from internal User model
     */
    public static BitbucketGetCurrentUserUserRes fromInternalModel(User user) {
        if (user == null) {
            return null;
        }

        BitbucketGetCurrentUserUserRes userRes = new BitbucketGetCurrentUserUserRes();
        userRes.setUuid(user.getId());
        userRes.setUsername(user.getUsername());
        userRes.setDisplayName(user.getDisplayName());

        // Build links from user URLs
        if (user.getAvatarUrl() != null || user.getUrl() != null) {
            BitbucketGetCurrentUserLinkRes avatar = user.getAvatarUrl() != null ?
                    new BitbucketGetCurrentUserLinkRes(null, user.getAvatarUrl()) : null;
            BitbucketGetCurrentUserLinkRes html = user.getUrl() != null ?
                    new BitbucketGetCurrentUserLinkRes(null, user.getUrl()) : null;
            userRes.setLinks(new BitbucketGetCurrentUserLinksRes(avatar, html, null));
        }

        return userRes;
    }
}


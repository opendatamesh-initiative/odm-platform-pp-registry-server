package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listmembers;

import org.opendatamesh.platform.pp.registry.githandler.model.User;

public abstract class BitbucketListMembersMapper {

    /**
     * Maps BitbucketListMembersUserRes to internal User model
     */
    public static User toInternalModel(BitbucketListMembersUserRes userRes) {
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
     * Maps BitbucketListMembersUserRes to internal User model (from workspace membership)
     * Uses nickname as username, falls back to accountId if nickname is null
     */
    public static User toInternalModelFromMembership(BitbucketListMembersUserRes userRes) {
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

        // Use nickname as username, fallback to accountId if nickname is null
        String username = userRes.getNickname();
        if (username == null || username.isEmpty()) {
            username = userRes.getAccountId();
        }

        return new User(userRes.getUuid(), username, userRes.getDisplayName(), avatarUrl, htmlUrl);
    }

    /**
     * Creates BitbucketListMembersUserRes from internal User model
     */
    public static BitbucketListMembersUserRes fromInternalModel(User user) {
        if (user == null) {
            return null;
        }
        
        BitbucketListMembersUserRes userRes = new BitbucketListMembersUserRes();
        userRes.setUuid(user.getId());
        userRes.setUsername(user.getUsername());
        userRes.setDisplayName(user.getDisplayName());
        
        // Build links from user URLs
        if (user.getAvatarUrl() != null || user.getUrl() != null) {
            BitbucketListMembersLinkRes avatar = user.getAvatarUrl() != null ? 
                new BitbucketListMembersLinkRes(null, user.getAvatarUrl()) : null;
            BitbucketListMembersLinkRes html = user.getUrl() != null ? 
                new BitbucketListMembersLinkRes(null, user.getUrl()) : null;
            userRes.setLinks(new BitbucketListMembersLinksRes(avatar, html, null));
        }
        
        return userRes;
    }
}


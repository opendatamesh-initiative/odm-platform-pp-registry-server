package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags;

import org.opendatamesh.platform.pp.registry.githandler.model.Tag;

public abstract class BitbucketListTagsMapper {

    /**
     * Maps BitbucketListTagsTagRes to internal Tag model
     */
    public static Tag toInternalModel(BitbucketListTagsTagRes tagRes) {
        if (tagRes == null) {
            return null;
        }

        String commitHash = null;
        if (tagRes.getTarget() != null) {
            commitHash = tagRes.getTarget().getHash();
        }

        return new Tag(tagRes.getName(), commitHash);
    }
}


package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listtags;

import org.opendatamesh.platform.pp.registry.githandler.model.Tag;

public abstract class GitHubListTagsMapper {

    public static Tag toInternalModel(GitHubListTagsTagRes tagRes) {
        if (tagRes == null) {
            return null;
        }

        return new Tag(
                tagRes.getName(),
                tagRes.getCommit() != null ? tagRes.getCommit().getSha() : null
        );
    }
}


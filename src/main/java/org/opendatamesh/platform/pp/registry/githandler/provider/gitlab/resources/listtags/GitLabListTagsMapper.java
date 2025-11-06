package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listtags;

import org.opendatamesh.platform.pp.registry.githandler.model.Tag;

public abstract class GitLabListTagsMapper {

    public static Tag toInternalModel(GitLabListTagsTagRes tagRes) {
        if (tagRes == null) {
            return null;
        }

        // Use commit hash from commit object, fallback to target field if commit is null
        String commitHash = tagRes.getCommit() != null ?
                tagRes.getCommit().getId() : tagRes.getTarget();

        return new Tag(
                tagRes.getName(),
                commitHash
        );
    }
}


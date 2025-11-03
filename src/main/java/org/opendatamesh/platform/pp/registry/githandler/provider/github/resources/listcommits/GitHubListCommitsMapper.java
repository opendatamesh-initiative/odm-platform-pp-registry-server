package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits;

import org.opendatamesh.platform.pp.registry.githandler.model.Commit;

public abstract class GitHubListCommitsMapper {

    public static Commit toInternalModel(GitHubListCommitsCommitRes commitRes) {
        if (commitRes == null) {
            return null;
        }

        if (commitRes.getCommit() == null || commitRes.getCommit().getAuthor() == null) {
            return null;
        }

        return new Commit(
                commitRes.getSha(),
                commitRes.getCommit().getMessage(),
                commitRes.getCommit().getAuthor().getEmail(),
                commitRes.getCommit().getAuthor().getDate()
        );
    }
}


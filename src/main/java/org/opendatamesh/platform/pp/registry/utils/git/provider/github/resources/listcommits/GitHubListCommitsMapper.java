package org.opendatamesh.platform.pp.registry.utils.git.provider.github.resources.listcommits;

import org.opendatamesh.platform.pp.registry.utils.git.model.Commit;

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
                commitRes.getCommit().getAuthor().getName(),
                commitRes.getCommit().getAuthor().getEmail(),
                commitRes.getCommit().getAuthor().getDate()
        );
    }

    public static Commit toInternalModel(GitHubCompareCommitsRes.CompareCommitRes commitRes) {
        if (commitRes == null) {
            return null;
        }
        return new Commit(
                commitRes.getSha(),
                commitRes.getCommit().getMessage(),
                commitRes.getCommit().getAuthor().getName(),
                commitRes.getCommit().getAuthor().getEmail(),
                commitRes.getCommit().getAuthor().getDate()
        );
    }
}


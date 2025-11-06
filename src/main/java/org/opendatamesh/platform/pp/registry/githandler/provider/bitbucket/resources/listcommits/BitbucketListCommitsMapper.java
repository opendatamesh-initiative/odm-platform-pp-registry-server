package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits;

import org.opendatamesh.platform.pp.registry.githandler.model.Commit;

public abstract class BitbucketListCommitsMapper {

    /**
     * Maps BitbucketListCommitsCommitRes to internal Commit model
     */
    public static Commit toInternalModel(BitbucketListCommitsCommitRes commitRes) {
        if (commitRes == null) {
            return null;
        }
        
        // Handle case where author user might be null
        String authorId = null;
        if (commitRes.getAuthor() != null && commitRes.getAuthor().getUser() != null) {
            authorId = commitRes.getAuthor().getUser().getAccountId();
        }

        return new Commit(commitRes.getHash(), commitRes.getMessage(), authorId, commitRes.getDate());
    }
}


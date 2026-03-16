package org.opendatamesh.platform.pp.registry.utils.git.provider.azure.resources.listcommits;

import org.opendatamesh.platform.pp.registry.utils.git.model.Commit;

public abstract class AzureListCommitsMapper {

    public static Commit toInternalModel(AzureListCommitsCommitRes commitRes) {
        if (commitRes == null) {
            return null;
        }

        if (commitRes.getAuthor() == null) {
            return null;
        }

        return new Commit(
                commitRes.getCommitId(),
                commitRes.getComment(),
                commitRes.getAuthor().getName(),
                commitRes.getAuthor().getEmail(),
                commitRes.getAuthor().getDate()
        );
    }
}


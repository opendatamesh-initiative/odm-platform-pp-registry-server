package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits;

import org.opendatamesh.platform.pp.registry.githandler.model.Commit;

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
                commitRes.getAuthor().getEmail(),
                commitRes.getAuthor().getDate()
        );
    }
}


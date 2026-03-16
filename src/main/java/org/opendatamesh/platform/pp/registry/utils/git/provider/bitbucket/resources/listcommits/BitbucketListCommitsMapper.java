package org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.resources.listcommits;

import org.opendatamesh.platform.pp.registry.utils.git.model.Commit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BitbucketListCommitsMapper {

    private static final Pattern AUTHOR_EMAIL_PATTERN = Pattern.compile("<([^>]+)>");

    /**
     * Maps BitbucketListCommitsCommitRes to internal Commit model
     */
    public static Commit toInternalModel(BitbucketListCommitsCommitRes commitRes) {
        if (commitRes == null) {
            return null;
        }

        // Handle case where author user might be null
        String authorId = null;
        String authorEmail = null;
        if (commitRes.getAuthor() != null) {
            if (commitRes.getAuthor().getUser() != null) {
                authorId = commitRes.getAuthor().getUser().getAccountId();
            }
            // Bitbucket provides author as "Display Name <email@example.com>" in author.raw
            String raw = commitRes.getAuthor().getRaw();
            if (raw != null) {
                Matcher m = AUTHOR_EMAIL_PATTERN.matcher(raw);
                if (m.find()) {
                    authorEmail = m.group(1);
                }
            }
        }

        return new Commit(commitRes.getHash(), commitRes.getMessage(), authorId, authorEmail, commitRes.getDate());
    }
}


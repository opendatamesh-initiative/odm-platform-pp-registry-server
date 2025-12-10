package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.comparator;

import org.opendatamesh.platform.pp.registry.githandler.model.Commit;

import java.util.Comparator;
import java.util.Date;

/**
 * Comparator for ordering commits in reverse chronological order (newest first).
 * Commits are sorted by their commit date in descending order.
 */
public class GitLabCommitComparator implements Comparator<Commit> {

    @Override
    public int compare(Commit commit1, Commit commit2) {
        if (commit1 == null && commit2 == null) {
            return 0;
        }
        if (commit1 == null) {
            return 1;
        }
        if (commit2 == null) {
            return -1;
        }

        Date date1 = commit1.getCommitDate();
        Date date2 = commit2.getCommitDate();

        if (date1 == null && date2 == null) {
            return 0;
        }
        if (date1 == null) {
            return 1;
        }
        if (date2 == null) {
            return -1;
        }

        // Reverse chronological order: newest first (descending order)
        return date2.compareTo(date1);
    }
}
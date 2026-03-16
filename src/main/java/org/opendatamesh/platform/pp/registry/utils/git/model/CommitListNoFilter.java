package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * No filter: list commits on the default branch.
 */
public record CommitListNoFilter() implements CommitListFilter {
    private static final CommitListNoFilter INSTANCE = new CommitListNoFilter();

    public static CommitListNoFilter getInstance() {
        return INSTANCE;
    }
}

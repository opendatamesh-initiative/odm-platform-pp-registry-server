package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * Filter: list commits between two refs (from..to). Both refs must be non-null.
 */
public record CommitListRangeFilter(CommitRef from, CommitRef to) implements CommitListFilter {
    public CommitListRangeFilter {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to must not be null");
        }
    }
}

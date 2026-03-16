package org.opendatamesh.platform.pp.registry.utils.git.model;

/**
 * Filter for listing commits: no filter, single branch, or range between two refs.
 * Providers dispatch on the concrete type to choose the right API (list default, list by branch, or compare).
 */
public sealed interface CommitListFilter permits CommitListNoFilter, CommitListSingleBranchFilter, CommitListRangeFilter {
}

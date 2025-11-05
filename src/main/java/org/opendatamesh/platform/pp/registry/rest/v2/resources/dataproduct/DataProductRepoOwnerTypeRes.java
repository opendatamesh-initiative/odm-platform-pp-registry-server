package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import java.util.Locale;

/**
 * Enum defining supported repository owner types
 */
public enum DataProductRepoOwnerTypeRes {
    ORGANIZATION,
    ACCOUNT;

    public static DataProductRepoOwnerTypeRes fromString(String value) {
        return DataProductRepoOwnerTypeRes.valueOf(value.toUpperCase(Locale.ROOT));
    }
}


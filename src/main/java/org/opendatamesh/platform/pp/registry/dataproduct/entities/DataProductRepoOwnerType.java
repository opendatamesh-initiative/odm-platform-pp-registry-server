package org.opendatamesh.platform.pp.registry.dataproduct.entities;

import java.util.Locale;

/**
 * Enum defining supported repository owner types
 */
public enum DataProductRepoOwnerType {
    ORGANIZATION,
    ACCOUNT;

    public static DataProductRepoOwnerType fromString(String value) {
        return DataProductRepoOwnerType.valueOf(value.toUpperCase(Locale.ROOT));
    }
}


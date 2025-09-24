package org.opendatamesh.platform.pp.registry.dataproduct.entities;

import java.util.Locale;

/**
 * Enum defining supported Git provider types
 */
public enum DataProductRepoProviderType {
    AZURE,
    BITBUCKET,
    GITHUB,
    GITLAB;

    public static DataProductRepoProviderType fromString(String value) {
        return DataProductRepoProviderType.valueOf(value.toUpperCase(Locale.ROOT));
    }
}

package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import java.util.Locale;

/**
 * Enum defining supported Git provider types
 */
public enum DataProductRepoProviderTypeRes {
    AZURE,
    BITBUCKET,
    GITHUB,
    GITLAB;

    public static DataProductRepoProviderTypeRes fromString(String value) {
        return DataProductRepoProviderTypeRes.valueOf(value.toUpperCase(Locale.ROOT));
    }
}

package org.opendatamesh.platform.pp.registry.dataproduct.resources;

import java.util.Locale;

/**
 * Enum defining supported Git provider types
 */
public enum ProviderType {
    AWS,
    AZURE,
    BITBUCKET,
    GITHUB,
    GITLAB;

    public static ProviderType fromString(String value) {
        return ProviderType.valueOf(value.toUpperCase(Locale.ROOT));
    }
}

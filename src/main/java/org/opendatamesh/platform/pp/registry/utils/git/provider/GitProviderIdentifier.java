package org.opendatamesh.platform.pp.registry.utils.git.provider;

public record GitProviderIdentifier(
        String type,
        String baseUrl
) {
}

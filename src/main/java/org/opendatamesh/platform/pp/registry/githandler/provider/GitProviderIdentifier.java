package org.opendatamesh.platform.pp.registry.githandler.provider;

public record GitProviderIdentifier(
        String type,
        String baseUrl
) {
}

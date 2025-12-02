package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.springframework.http.HttpHeaders;


public interface GitProviderFactory {
    GitProvider buildGitProvider(
            GitProviderIdentifier providerIdentifier,
            HttpHeaders headers
    ) throws BadRequestException;

    GitProvider buildUnauthenticatedGitProvider(
            GitProviderIdentifier providerIdentifier
    ) throws BadRequestException;

}

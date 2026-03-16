package org.opendatamesh.platform.pp.registry.utils.git.provider;

import org.springframework.http.HttpHeaders;


public interface GitProviderFactory {
    GitProvider buildGitProvider(
            GitProviderIdentifier providerIdentifier,
            HttpHeaders headers
    );

    GitProviderExtension buildGitProviderExtension(GitProviderIdentifier providerIdentifier);

}

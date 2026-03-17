package org.opendatamesh.platform.pp.registry.git.provider;

import org.opendatamesh.platform.git.provider.GitProvider;
import org.opendatamesh.platform.git.provider.GitProviderExtension;
import org.opendatamesh.platform.git.provider.GitProviderIdentifier;
import org.springframework.http.HttpHeaders;

public interface GitProviderFactory {
    GitProvider buildGitProvider(
            GitProviderIdentifier providerIdentifier,
            HttpHeaders headers
    );

    GitProviderExtension buildGitProviderExtension(GitProviderIdentifier providerIdentifier);
}

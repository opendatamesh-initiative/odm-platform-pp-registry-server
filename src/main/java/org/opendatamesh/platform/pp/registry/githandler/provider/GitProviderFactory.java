package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


public interface GitProviderFactory {
    public Optional<GitProvider> getProvider(
            DataProductRepoProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            Credential credential
    );
}

package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.GitHubProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.GitLabProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


public abstract class GitProviderFactory {

    public static Optional<GitProvider> getProvider(
            DataProductRepoProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            Credential credential
    ) {
        switch (type) {
            case GITHUB:
                return Optional.of(new GitHubProvider(
                        baseUrl,
                        restTemplate,
                        credential
                ));
            case GITLAB:
                return Optional.of(new GitLabProvider(
                        baseUrl,
                        restTemplate,
                        credential
                ));
            case BITBUCKET:
                return Optional.of(new BitbucketProvider(
                        baseUrl,
                        restTemplate,
                        credential
                ));
            case AZURE:
                return Optional.of(new AzureDevOpsProvider(
                        baseUrl,
                        restTemplate,
                        credential
                ));
            default:
                return Optional.empty();

        }
    }
}

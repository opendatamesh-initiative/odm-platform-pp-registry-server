package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.GitHubProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.GitLabProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class GitProviderFactoryImpl implements GitProviderFactory {

    public Optional<GitProvider> getProvider(
            DataProductRepoProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            Credential credential
    ) {
        return switch (type) {
            case GITHUB -> Optional.of(new GitHubProvider(
                    baseUrl,
                    restTemplate,
                    credential
            ));
            case GITLAB -> Optional.of(new GitLabProvider(
                    baseUrl,
                    restTemplate,
                    credential
            ));
            case BITBUCKET -> Optional.of(new BitbucketProvider(
                    baseUrl,
                    restTemplate,
                    credential
            ));
            case AZURE -> Optional.of(new AzureDevOpsProvider(
                    baseUrl,
                    restTemplate,
                    credential
            ));
            default -> Optional.empty();
        };
    }

}

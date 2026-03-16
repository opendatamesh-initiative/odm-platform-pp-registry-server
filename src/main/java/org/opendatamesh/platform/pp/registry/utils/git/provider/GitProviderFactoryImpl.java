package org.opendatamesh.platform.pp.registry.utils.git.provider;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitProviderConfigurationException;
import org.opendatamesh.platform.pp.registry.utils.git.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.pp.registry.utils.git.provider.azure.credentials.AzureCredentialFactory;
import org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.pp.registry.utils.git.provider.bitbucket.credentials.BitbucketCredentialFactory;
import org.opendatamesh.platform.pp.registry.utils.git.provider.github.GitHubProvider;
import org.opendatamesh.platform.pp.registry.utils.git.provider.github.credentials.GitHubCredentialFactory;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.GitLabProvider;
import org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.credentials.GitLabCredentialFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitProviderFactoryImpl implements GitProviderFactory {

    private final RestTemplate restTemplate;

    public GitProviderFactoryImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public GitProvider buildGitProvider(
            GitProviderIdentifier providerIdentifier,
            HttpHeaders headers
    ) {
        DataProductRepoProviderType providerType = getProviderType(providerIdentifier);
        return switch (providerType) {
            case GITHUB -> new GitHubProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    GitHubCredentialFactory.createCredentials(headers)
            );
            case GITLAB -> new GitLabProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    GitLabCredentialFactory.createCredentials(headers)
            );
            case BITBUCKET -> new BitbucketProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    BitbucketCredentialFactory.createCredentials(headers)
            );
            case AZURE -> new AzureDevOpsProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    AzureCredentialFactory.createCredentials(headers)
            );
        };
    }

    @Override
    public GitProviderExtension buildGitProviderExtension(GitProviderIdentifier providerIdentifier) {
        DataProductRepoProviderType providerType = getProviderType(providerIdentifier);
        return switch (providerType) {
            case BITBUCKET -> new BitbucketProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    null
            );
            default -> (GitProviderModelResourceType modelResourceType) -> {
                throw new GitProviderConfigurationException("Git provider type: " + providerIdentifier.type()
                        + " does not support custom resource definitions");
            };
        };
    }

    private DataProductRepoProviderType getProviderType(GitProviderIdentifier providerIdentifier) {
        if (providerIdentifier == null) {
            throw new GitProviderConfigurationException("GitProviderIdentifier cannot be null");
        }
        if (providerIdentifier.type() == null) {
            throw new GitProviderConfigurationException("Git provider type cannot be null");
        }
        DataProductRepoProviderType providerType;
        try {
            providerType = DataProductRepoProviderType.valueOf(providerIdentifier.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GitProviderConfigurationException("Unsupported Git provider type: " + providerIdentifier.type());
        }
        return providerType;
    }
}

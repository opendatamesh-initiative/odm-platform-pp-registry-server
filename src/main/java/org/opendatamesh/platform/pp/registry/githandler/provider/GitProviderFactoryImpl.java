package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.credentials.AzureCredentialFactory;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.credentials.BitbucketCredentialFactory;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.GitHubProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.credentials.GitHubCredentialFactory;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.GitLabProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.credentials.GitLabCredentialFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitProviderFactoryImpl implements GitProviderFactory {

    private final RestTemplate restTemplate;

    @Autowired
    public GitProviderFactoryImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public GitProvider buildGitProvider(
            GitProviderIdentifier providerIdentifier,
            HttpHeaders headers
    ) throws BadRequestException {
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
    public GitProvider buildUnauthenticatedGitProvider(GitProviderIdentifier providerIdentifier) throws BadRequestException {
        DataProductRepoProviderType providerType = getProviderType(providerIdentifier);
        return switch (providerType) {
            case GITHUB -> new GitHubProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    null
            );
            case GITLAB -> new GitLabProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    null
            );
            case BITBUCKET -> new BitbucketProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    null
            );
            case AZURE -> new AzureDevOpsProvider(
                    providerIdentifier.baseUrl(),
                    restTemplate,
                    null
            );
        };
    }

    private DataProductRepoProviderType getProviderType(GitProviderIdentifier providerIdentifier) {
        if (providerIdentifier == null) {
            throw new BadRequestException("GitProviderIdentifier cannot be null");
        }
        if (providerIdentifier.type() == null) {
            throw new BadRequestException("Git provider type cannot be null");
        }
        DataProductRepoProviderType providerType;
        try {
            providerType = DataProductRepoProviderType.valueOf(providerIdentifier.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported Git provider type: " + providerIdentifier.type());
        }
        return providerType;
    }
}

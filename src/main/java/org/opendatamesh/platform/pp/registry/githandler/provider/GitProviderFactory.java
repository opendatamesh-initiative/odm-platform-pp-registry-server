package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.GitHubProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.GitLabProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitProviderFactory {

    public GitProvider getProvider(
            DataProductRepoProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            PatCredential patCredential
    ) {
        switch (type) {
            case GITHUB:
                return new GitHubProvider(baseUrl, restTemplate, patCredential);
            case GITLAB:
                return new GitLabProvider(baseUrl, restTemplate, patCredential);
            case BITBUCKET:
                return new BitbucketProvider(baseUrl, restTemplate, patCredential);
            case AZURE:
                return new AzureDevOpsProvider(baseUrl, restTemplate, patCredential);
            default:
                throw new IllegalArgumentException("Unsupported provider type: " + type);
        }
    }

}

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

@Component
public class GitProviderFactory {

    public static GitProvider getProvider(
            DataProductRepoProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            Credential credential
    ) {
        switch (type) {
            case GITHUB:
                return new GitHubProvider(
                        baseUrl,
                        restTemplate,
                        credential
                );
            case GITLAB:
                return new GitLabProvider(
                        baseUrl,
                        restTemplate,
                        credential
                );
            case BITBUCKET:
                return new BitbucketProvider(
                        baseUrl,
                        restTemplate,
                        credential
                );
            case AZURE:
                if (!(credential instanceof PatCredential)) {
                    throw new IllegalArgumentException("AzureDevOpsProvider supports only PatCredential");
                }
                return new AzureDevOpsProvider(
                        baseUrl,
                        restTemplate,
                        (PatCredential) credential
                );
            default:
                throw new IllegalArgumentException("Unsupported provider type: " + type);

        }
    }
}

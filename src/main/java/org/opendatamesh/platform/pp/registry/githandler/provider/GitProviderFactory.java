package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.opendatamesh.platform.pp.registry.dataproduct.resources.ProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.AwsCredential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.OauthCredential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.aws.AwsCodeCommitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.GitHubProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.GitLabProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GitProviderFactory {

    public static GitProvider getProvider(
            ProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            PatCredential patCredential,
            OauthCredential oauthCredential,
            AwsCredential awsCredential
    ) {
        switch (type) {
            case GITHUB:
                return new GitHubProvider(baseUrl, restTemplate, patCredential, oauthCredential);
            case GITLAB:
                return new GitLabProvider(baseUrl, restTemplate, patCredential, oauthCredential);
            case BITBUCKET:
                return new BitbucketProvider(baseUrl, restTemplate, patCredential, oauthCredential);
            case AWS:
                return new AwsCodeCommitProvider(baseUrl, restTemplate, awsCredential);
            case AZURE:
                return new AzureDevOpsProvider(baseUrl, restTemplate, patCredential);
            default:
                throw new IllegalArgumentException("Unsupported provider type: " + type);
        }
    }

    public static GitProvider getProvider(
            String type,
            String baseUrl,
            RestTemplate restTemplate,
            PatCredential patCredential,
            OauthCredential oauthCredential,
            AwsCredential awsCredential
    ) {
        return getProvider(ProviderType.fromString(type), baseUrl, restTemplate, patCredential, oauthCredential, awsCredential);
    }
}

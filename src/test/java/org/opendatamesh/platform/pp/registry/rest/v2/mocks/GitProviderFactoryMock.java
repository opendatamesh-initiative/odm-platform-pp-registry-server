package org.opendatamesh.platform.pp.registry.rest.v2.mocks;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@Primary
@Profile("test")
public class GitProviderFactoryMock implements GitProviderFactory {

    private GitProvider mockGitProvider;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Optional<GitProvider> getProvider(DataProductRepoProviderType type, String baseUrl, RestTemplate restTemplate, Credential credential) {
        if (mockGitProvider != null)  {
            return Optional.of(mockGitProvider);
        }
        logger.warn("Calling factory without properly setting the mock");
        return Optional.empty();
    }

    public void setMockGitProvider(GitProvider mockGitProvider) {
        this.mockGitProvider = mockGitProvider;
    }
}

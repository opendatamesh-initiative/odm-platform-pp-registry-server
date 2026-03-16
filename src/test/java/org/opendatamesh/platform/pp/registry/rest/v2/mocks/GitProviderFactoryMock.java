package org.opendatamesh.platform.pp.registry.rest.v2.mocks;

import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitProviderConfigurationException;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderExtension;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GitProviderFactoryMock extends IntegrationMock implements GitProviderFactory {

    private GitProvider mockGitProvider;
    private GitProviderExtension mockGitProviderExtension;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void reset() {
        mockGitProvider = Mockito.mock(GitProvider.class);
        mockGitProviderExtension = Mockito.mock(GitProviderExtension.class);
    }

    @Override
    public GitProvider buildGitProvider(GitProviderIdentifier providerIdentifier, HttpHeaders headers) {
        if (mockGitProvider != null) {
            return mockGitProvider;
        }
        logger.warn("Calling factory without properly setting the mock");
        throw new GitProviderConfigurationException("Mock GitProvider not set");
    }

    @Override
    public GitProviderExtension buildGitProviderExtension(GitProviderIdentifier providerIdentifier) {
        if (mockGitProviderExtension != null) {
            return mockGitProviderExtension;
        }
        logger.warn("Calling factory without properly setting the mock");
        throw new GitProviderConfigurationException("Mock GitProviderExtension not set");
    }

    public void setMockGitProvider(GitProvider mockGitProvider) {
        this.mockGitProvider = mockGitProvider;
    }

    public GitProvider getMockGitProvider() {
        return mockGitProvider;
    }

    public GitProviderExtension getMockGitProviderExtension() {
        return mockGitProviderExtension;
    }

}

package org.opendatamesh.platform.pp.registry.test.mocks;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepoProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.mockito.Mockito;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Mock implementation of GitProviderFactory for testing.
 * This replaces the real GitProviderFactory in tests and returns a mock GitProvider.
 */
@Profile("test")
@Component
@Primary
public class GitProviderFactoryMock implements GitProviderFactory {

    private GitProvider mockGitProvider;

    public GitProviderFactoryMock() {
        reset();
    }

    @Override
    public Optional<GitProvider> getProvider(
            DataProductRepoProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            PatCredential patCredential
    ) {
        // Always return the mock GitProvider for tests
        return Optional.of(mockGitProvider);
    }

    public void reset() {
        mockGitProvider = Mockito.mock(GitProvider.class);
    }

    public GitProvider getMockGitProvider() {
        return mockGitProvider;
    }
}
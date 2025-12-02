package org.opendatamesh.platform.pp.registry.rest.v2.mocks;

import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderIdentifier;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GitProviderFactoryMock extends IntegrationMock implements GitProviderFactory {

    private GitProvider mockGitProvider;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void reset() {
        mockGitProvider = Mockito.mock(GitProvider.class);
        
        // Create a mock GitAuthContext
        GitAuthContext mockAuthContext = new GitAuthContext();
        mockAuthContext.setTransportProtocol(GitAuthContext.TransportProtocol.HTTP);
        
        // Mock the createGitAuthContext method to return our mock context
        Mockito.when(mockGitProvider.createGitAuthContext()).thenReturn(mockAuthContext);
    }

    @Override
    public GitProvider buildGitProvider(GitProviderIdentifier providerIdentifier, HttpHeaders headers) throws BadRequestException {
        if (mockGitProvider != null) {
            return mockGitProvider;
        }
        logger.warn("Calling factory without properly setting the mock");
        throw new BadRequestException("Mock GitProvider not set");
    }

    @Override
    public GitProvider buildUnauthenticatedGitProvider(GitProviderIdentifier providerIdentifier) throws BadRequestException {
        if (mockGitProvider != null) {
            return mockGitProvider;
        }
        logger.warn("Calling factory without properly setting the mock");
        throw new BadRequestException("Mock GitProvider not set");
    }

    public void setMockGitProvider(GitProvider mockGitProvider) {
        this.mockGitProvider = mockGitProvider;
        
        // Ensure the GitAuthContext is properly mocked
        if (mockGitProvider != null) {
            GitAuthContext mockAuthContext = new GitAuthContext();
            mockAuthContext.setTransportProtocol(GitAuthContext.TransportProtocol.HTTP);
            Mockito.when(mockGitProvider.createGitAuthContext()).thenReturn(mockAuthContext);
        }
    }

    public GitProvider getMockGitProvider() {
        return mockGitProvider;
    }
}

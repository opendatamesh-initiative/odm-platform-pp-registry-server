package org.opendatamesh.platform.pp.registry.rest.v2.mocks;

import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperationFactory;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GitOperationFactoryMock extends IntegrationMock implements GitOperationFactory {

    private GitOperation mockGitOperation;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void reset() {
        mockGitOperation = Mockito.mock(GitOperation.class);
    }

    @Override
    public GitOperation createGitOperation() {
        if (mockGitOperation != null) {
            return mockGitOperation;
        }
        logger.warn("Calling factory without properly setting the mock");
        return Mockito.mock(GitOperation.class);
    }

    @Override
    public GitOperation createGitOperation(GitAuthContext authContext) {
        if (mockGitOperation != null) {
            return mockGitOperation;
        }
        logger.warn("Calling factory without properly setting the mock");
        return Mockito.mock(GitOperation.class);
    }

    public void setMockGitOperation(GitOperation mockGitOperation) {
        this.mockGitOperation = mockGitOperation;
    }

    public GitOperation getMockGitOperation() {
        return mockGitOperation;
    }
}

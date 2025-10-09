package org.opendatamesh.platform.pp.registry.githandler.git;

import org.springframework.stereotype.Component;

/**
 * Default implementation of GitOperationFactory
 */
@Component
public class GitOperationFactoryImpl implements GitOperationFactory {

    @Override
    public GitOperation createGitOperation() {
        return new GitOperationImpl();
    }

    @Override
    public GitOperation createGitOperation(GitAuthContext authContext) {
        return new GitOperationImpl(authContext);
    }
}

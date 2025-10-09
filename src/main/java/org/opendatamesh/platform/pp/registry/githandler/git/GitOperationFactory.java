package org.opendatamesh.platform.pp.registry.githandler.git;

/**
 * Factory for creating GitOperation instances
 * This factory pattern hides the implementation details and provides a clean interface
 */
public interface GitOperationFactory {

    /**
     * Creates a new GitOperation instance
     *
     * @return a new GitOperation instance
     */
    GitOperation createGitOperation();

    /**
     * Creates a new GitOperation instance with authentication context
     *
     * @param authContext the authentication context for git operations
     * @return a new GitOperation instance
     */
    GitOperation createGitOperation(GitAuthContext authContext);
}

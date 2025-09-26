package org.opendatamesh.platform.pp.registry.githandler.git;

/**
 * Factory for creating GitOperation instances
 * This factory pattern hides the implementation details and provides a clean interface
 */
public abstract class GitOperationFactory {

    /**
     * Creates a new GitOperation instance
     *
     * @return a new GitOperation instance
     */
    public static GitOperation createGitOperation() {
        return new GitOperationImpl();
    }

    /**
     * Private constructor to prevent instantiation
     */
    private GitOperationFactory() {
        // Factory class - no instantiation needed
    }
}

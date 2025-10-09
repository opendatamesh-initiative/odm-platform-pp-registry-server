package org.opendatamesh.platform.pp.registry.githandler.exceptions;

/**
 * Checked exception for Git operation failures.
 * This exception should be caught and handled appropriately by the calling code.
 * It wraps various Git-related errors (GitAPIException, IOException, etc.) into a single
 * checked exception type that can be handled based on the specific use case.
 */
public class GitOperationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String operation;
    private final String details;
    
    /**
     * Creates a new GitOperationException with a message.
     *
     * @param message the error message
     */
    public GitOperationException(String message) {
        super(message);
        this.operation = null;
        this.details = null;
    }
    
    /**
     * Creates a new GitOperationException with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public GitOperationException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.details = null;
    }
    
    /**
     * Creates a new GitOperationException with operation context.
     *
     * @param operation the Git operation that failed
     * @param message the error message
     */
    public GitOperationException(String operation, String message) {
        super(String.format("Git operation '%s' failed: %s", operation, message));
        this.operation = operation;
        this.details = message;
    }
    
    /**
     * Creates a new GitOperationException with operation context and cause.
     *
     * @param operation the Git operation that failed
     * @param message the error message
     * @param cause the underlying cause
     */
    public GitOperationException(String operation, String message, Throwable cause) {
        super(String.format("Git operation '%s' failed: %s", operation, message), cause);
        this.operation = operation;
        this.details = message;
    }
    
    /**
     * Gets the operation that failed.
     *
     * @return the operation name, or null if not specified
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Gets the operation details.
     *
     * @return the operation details, or null if not specified
     */
    public String getDetails() {
        return details;
    }
    
    /**
     * Checks if this exception has operation context.
     *
     * @return true if operation context is available
     */
    public boolean hasOperationContext() {
        return operation != null;
    }
}

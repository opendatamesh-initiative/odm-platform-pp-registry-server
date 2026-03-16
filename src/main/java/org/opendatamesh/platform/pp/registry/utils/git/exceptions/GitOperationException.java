package org.opendatamesh.platform.pp.registry.utils.git.exceptions;

/**
 * Runtime exception for low-level Git operation failures (clone, init, add, commit, push, tag, etc.).
 * Wraps Git-related errors (GitAPIException, IOException, etc.) with optional operation context.
 * Caught by services or by the REST exception handler and mapped to an appropriate HTTP response.
 */
public class GitOperationException extends GitException {

    private final String operation;
    private final String details;


    public GitOperationException(String message) {
        super(message);
        this.operation = null;
        this.details = null;
    }

    public GitOperationException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.details = null;
    }

    public GitOperationException(String operation, String message) {
        super(String.format("Git operation '%s' failed: %s", operation, message));
        this.operation = operation;
        this.details = message;
    }

    public GitOperationException(String operation, String message, Throwable cause) {
        super(String.format("Git operation '%s' failed: %s", operation, message), cause);
        this.operation = operation;
        this.details = message;
    }


    public String getOperation() {
        return operation;
    }


    public String getDetails() {
        return details;
    }

    public boolean hasOperationContext() {
        return operation != null;
    }
}

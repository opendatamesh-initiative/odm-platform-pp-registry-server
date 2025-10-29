package org.opendatamesh.platform.pp.registry.githandler.exceptions;

/**
 * Exception thrown when authentication with a Git provider fails.
 * This exception is used instead of propagating 401 status codes from external providers
 * to avoid logging out users from the current application.
 */
public class GitProviderAuthenticationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new GitProviderAuthenticationException with a message.
     *
     * @param message the error message describing the authentication problem
     */
    public GitProviderAuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Creates a new GitProviderAuthenticationException with a message and cause.
     *
     * @param message the error message describing the authentication problem
     * @param cause the underlying cause
     */
    public GitProviderAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}


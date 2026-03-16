package org.opendatamesh.platform.pp.registry.utils.git.exceptions;

/**
 * Exception thrown when authentication with a Git provider fails.
 * This exception is used instead of propagating 401 status codes from external providers
 * to avoid logging out users from the current application.
 */
public class GitProviderAuthenticationException extends GitException {

    public GitProviderAuthenticationException(String message) {
        super(message);
    }

    public GitProviderAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}


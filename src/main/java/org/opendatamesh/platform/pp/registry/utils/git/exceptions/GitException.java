package org.opendatamesh.platform.pp.registry.utils.git.exceptions;

/**
 * Base runtime exception for all Git-related failures (provider API errors,
 * authentication failures, and low-level Git operations).
 * Allows a single catch point and consistent handling for the git layer.
 */
public class GitException extends RuntimeException {

    public GitException(String message) {
        super(message);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}

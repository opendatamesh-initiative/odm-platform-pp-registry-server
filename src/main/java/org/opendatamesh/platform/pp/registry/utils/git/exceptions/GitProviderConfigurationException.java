package org.opendatamesh.platform.pp.registry.utils.git.exceptions;

/**
 * Thrown when git provider configuration is invalid (e.g. missing or unsupported
 * credentials, unsupported provider type). Consumers may map this to a 400 Bad Request.
 */
public class GitProviderConfigurationException extends GitException {

    public GitProviderConfigurationException(String message) {
        super(message);
    }

    public GitProviderConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

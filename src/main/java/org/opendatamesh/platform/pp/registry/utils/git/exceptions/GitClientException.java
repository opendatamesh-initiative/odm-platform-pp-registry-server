package org.opendatamesh.platform.pp.registry.utils.git.exceptions;

/**
 * Exception for Git provider HTTP API failures (e.g. 404, 403, 500 from GitHub, GitLab, etc.).
 * Carries the HTTP status code and optional response body for mapping to API responses.
 */
public class GitClientException extends GitException {

    private final int code;
    private final String responseBody;

    public GitClientException(int statusCode, String responseBody) {
        super(formatMessage(statusCode, responseBody));
        this.code = statusCode;
        this.responseBody = responseBody;
    }

    public GitClientException(int statusCode, String responseBody, Throwable cause) {
        super(formatMessage(statusCode, responseBody), cause);
        this.code = statusCode;
        this.responseBody = responseBody;
    }

    private static String formatMessage(int code, String responseBody) {
        String body = responseBody != null && responseBody.length() > 200
                ? responseBody.substring(0, 200) + "..."
                : responseBody;
        return "Git provider error: code=" + code + (body != null ? ", body=" + body : "");
    }

    public int getCode() {
        return code;
    }

    public String getResponseBody() {
        return responseBody;
    }
}

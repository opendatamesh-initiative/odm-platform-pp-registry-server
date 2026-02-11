package org.opendatamesh.platform.pp.registry.rest;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.RegistryApiException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.ErrorRes;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ConcurrencyFailureException.class})
    protected ResponseEntity<Object> handleConcurrencyConflict(ConcurrencyFailureException e, WebRequest request) {
        logger.info(e.getMessage());
        String url = getUrl(request);
        String message = "The resource is unavailable at the moment please retry";
        ErrorRes error = new ErrorRes(HttpStatus.CONFLICT.value(), "Concurrency", message, url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(e, error, headers, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({RegistryApiException.class})
    protected ResponseEntity<Object> handleNotificationApiException(RegistryApiException e, WebRequest request) {
        if (e.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
            logger.error(e.getErrorName() + ":" + e.getMessage(), e);
        } else {
            logger.info(e.getErrorName() + ":" + e.getMessage());
        }
        String url = getUrl(request);
        ErrorRes error = new ErrorRes(e.getStatus().value(), e.getErrorName(), e.getMessage(), url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(e, error, headers, e.getStatus(), request);
    }

    @ExceptionHandler({PropertyReferenceException.class})
    protected ResponseEntity<Object> handlePropertyReferenceException(PropertyReferenceException e, WebRequest request) {
        BadRequestException badRequestException = new BadRequestException(e.getMessage(), e);
        return handleNotificationApiException(badRequestException, request);
    }

    @ExceptionHandler(GitProviderAuthenticationException.class)
    public ResponseEntity<ErrorRes> handleGitProviderAuthenticationException(GitProviderAuthenticationException ex, WebRequest request) {
        ErrorRes error = ErrorRes.of(
                HttpStatus.BAD_REQUEST.value(),
                "Git Provider Authentication Failed",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientException.class)
    protected ResponseEntity<Object> handleGitProviderClientException(ClientException e, WebRequest request) {
        HttpStatus status;
        try {
            int code = e.getCode();
            if (code >= 100 && code < 600) {
                status = HttpStatus.valueOf(code);
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } catch (IllegalArgumentException ex) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (status.is5xxServerError()) {
            logger.error("GitProviderError: " + e.getResponseBody(), e);
        } else {
            logger.info("GitProviderError: " + e.getResponseBody());
        }
        String url = getUrl(request);
        String message = e.getResponseBody() != null ? e.getResponseBody() : e.getMessage();
        ErrorRes errorRes = new ErrorRes(status.value(), "GitProviderError", message, url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(e, errorRes, headers, status, request);
    }

    @ExceptionHandler({RuntimeException.class})
    protected ResponseEntity<Object> handleRuntimeException(RuntimeException e, WebRequest request) {
        logger.error("Unknown server error: ", e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String url = getUrl(request);
        ErrorRes errorRes = new ErrorRes(status.value(), "ServerError",
                "Unknown Internal Server Error", url);
        return handleExceptionInternal(e, errorRes, headers, status, request);
    }

    private String getUrl(WebRequest request) {
        String url = request.toString();
        if (request instanceof ServletWebRequest r) {
            url = r.getRequest().getRequestURI();
        }
        return url;
    }
}

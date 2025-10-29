package org.opendatamesh.platform.pp.registry.rest;

import org.opendatamesh.platform.pp.registry.exceptions.*;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.ClientException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitProviderAuthenticationException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflictException(ResourceConflictException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ErrorResponse> handleInternalException(InternalException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotImplemented.class)
    public ResponseEntity<ErrorResponse> handleNotImplemented(NotImplemented ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_IMPLEMENTED.value(),
                "Not Implemented",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(GitProviderAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleGitProviderAuthenticationException(GitProviderAuthenticationException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Git Provider Authentication Failed",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(ClientException ex, WebRequest request) {
        // Map ClientException status code to appropriate HTTP status
        HttpStatus httpStatus;
        String errorTitle;
        
        int statusCode = ex.getCode();
        if (statusCode >= 400 && statusCode < 500) {
            // Client errors (4xx)
            if (statusCode == 401) {
                httpStatus = HttpStatus.UNAUTHORIZED;
                errorTitle = "Unauthorized";
            } else if (statusCode == 403) {
                httpStatus = HttpStatus.FORBIDDEN;
                errorTitle = "Forbidden";
            } else if (statusCode == 404) {
                httpStatus = HttpStatus.NOT_FOUND;
                errorTitle = "Not Found";
            } else {
                httpStatus = HttpStatus.BAD_REQUEST;
                errorTitle = "Bad Request";
            }
        } else if (statusCode >= 500 && statusCode < 600) {
            // Server errors (5xx)
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            errorTitle = "Internal Server Error";
        } else {
            // Default to internal server error for unexpected status codes
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            errorTitle = "Internal Server Error";
        }
        
        ErrorResponse error = ErrorResponse.of(
                httpStatus.value(),
                errorTitle,
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, httpStatus);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

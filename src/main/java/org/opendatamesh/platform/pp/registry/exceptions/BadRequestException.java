package org.opendatamesh.platform.pp.registry.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RegistryException {

	private static final long serialVersionUID = -2775226019414735885L;
	private static final String ERROR_CODE = "BAD_REQUEST";

	public BadRequestException(String message) {
		super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
	}

	public BadRequestException(String message, Throwable cause) {
		super(message, ERROR_CODE, HttpStatus.BAD_REQUEST, cause);
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.BAD_REQUEST;
	}

}

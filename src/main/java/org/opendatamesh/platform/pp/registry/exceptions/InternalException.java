package org.opendatamesh.platform.pp.registry.exceptions;

import org.springframework.http.HttpStatus;

public class InternalException extends RegistryException {
	
	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "INTERNAL_ERROR";
	
	public InternalException(String message) {
		super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	public InternalException(String message, Throwable cause) {
		super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
}

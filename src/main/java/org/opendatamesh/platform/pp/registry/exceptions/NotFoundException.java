package org.opendatamesh.platform.pp.registry.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RegistryException {

	private static final long serialVersionUID = 2263265399328435820L;
	private static final String ERROR_CODE = "NOT_FOUND";

	public NotFoundException(String message) {
		super(message, ERROR_CODE, HttpStatus.NOT_FOUND);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, ERROR_CODE, HttpStatus.NOT_FOUND, cause);
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.NOT_FOUND;
	}

}

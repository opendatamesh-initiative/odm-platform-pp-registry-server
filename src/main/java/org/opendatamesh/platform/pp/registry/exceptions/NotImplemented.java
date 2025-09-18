package org.opendatamesh.platform.pp.registry.exceptions;

import org.springframework.http.HttpStatus;

public class NotImplemented extends RegistryException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "NOT_IMPLEMENTED";

	public NotImplemented(String message) {
		super(message, ERROR_CODE, HttpStatus.NOT_IMPLEMENTED);
	}

	public NotImplemented(String message, Throwable cause) {
		super(message, ERROR_CODE, HttpStatus.NOT_IMPLEMENTED, cause);
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.NOT_IMPLEMENTED;
	}
}

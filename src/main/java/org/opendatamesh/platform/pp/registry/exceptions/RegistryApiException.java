package org.opendatamesh.platform.pp.registry.exceptions;

import org.springframework.http.HttpStatus;

public abstract class RegistryApiException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3876573329263306459L;	
	
	public RegistryApiException() {
		super();
	}

	public RegistryApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RegistryApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public RegistryApiException(String message) {
		super(message);
	}

	public RegistryApiException(Throwable cause) {
		super(cause);
	}

	/**
	 * @return the errorName
	 */
	public String getErrorName() {
		return getClass().getSimpleName();	
	}

	/**
	 * @return the status
	 */
	public abstract HttpStatus getStatus();	
	

}
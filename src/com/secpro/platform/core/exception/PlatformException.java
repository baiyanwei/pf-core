package com.secpro.platform.core.exception;

public class PlatformException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1424038216344067102L;
	private String _exceptionMassage = null;

	public PlatformException(String cause, Throwable able) {
		super(cause, able);
		this._exceptionMassage = cause;
	}

	public PlatformException(String exceptionMassage) {
		super(exceptionMassage);
		this._exceptionMassage = exceptionMassage;
	}
	
	public String getExceptionMassage() {
		return _exceptionMassage;
	}

}

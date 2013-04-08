package com.viaplay.jcurl.exception;

/**
 * This exception class is an unchecked version of the FileNotFoundException.
 * 
 * @author mikael.p.larsson@afconsult.com
 * 
 */
public class JCurlFileNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3556181514904173314L;

	public JCurlFileNotFoundException() {
		super();
	}

	public JCurlFileNotFoundException(Throwable t) {
		super(t);
	}

}

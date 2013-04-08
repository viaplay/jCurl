/**
 * 
 */
package com.viaplay.jcurl.exception;

/**
 * This exception class is an unchecked version of the MalformedURLException.
 * 
 * @author mikael.p.larsson@afconsult.com
 *
 */
public class JCurlMalformedURLException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3556181514904173314L;

	public JCurlMalformedURLException() {
		super();
	}
	
	public JCurlMalformedURLException(Throwable t) {
		super(t);
	}
	
}

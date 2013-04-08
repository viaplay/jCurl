/**
 * 
 */
package com.viaplay.jcurl.exception;

/**
 * This exception class is an unchecked version of the SocketTimeoutException.
 * 
 * @author mikael.p.larsson@afconsult.com
 *
 */
public class JCurlSocketTimeoutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3556181514904173314L;

	public JCurlSocketTimeoutException() {
		super();
	}
	
	public JCurlSocketTimeoutException(Throwable t) {
		super(t);
	}
	
}

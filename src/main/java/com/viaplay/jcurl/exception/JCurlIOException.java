/**
 * 
 */
package com.viaplay.jcurl.exception;

/**
 * This exception class is an unchecked version of the IOException.
 * 
 * @author mikael.p.larsson@afconsult.com
 *
 */
public class JCurlIOException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3556181514904173314L;

	public JCurlIOException() {
		super();
	}
	
	public JCurlIOException(Throwable t) {
		super(t);
	}
	
}

package com.viaplay.jcurl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The JCurlResponse object has all parameters and the payload that is to be sent to a host. The fields in this class
 * conducts the behavior of JCurl.
 * 
 * @author mikael.p.larsson@afconsult.com
 * 
 */
public class JCurlRequest {
	public static final String HEAD = "HEAD";
	public static final String GET = "GET";
	public static final String PUT = "PUT";
	public static final String POST = "POST";
	public static final String DELETE = "DELETE";

	private String urlAsString = null;
	private URL url = null;
	private String method = "GET";
	private int timeOutMillis = 30000;
	private Map<String, String> properties = null;
	private String payload = null;
	private String charsetName = "UTF8";
	private boolean exceptionsToBeThrown = false;
	private JCurlCookieManager jCurlCookieManager = null;

	/**
	 * This constructor instantiates the object with an URL and urlAsString
	 * 
	 * @param url
	 *            The ULR object to the wanted resource.
	 */
	public JCurlRequest(URL url) {
		this.urlAsString = url.toExternalForm();
		this.url = url;
	}

	public JCurlRequest(URL url, JCurlCookieManager jCurlCookieManager) {
		this.urlAsString = url.toExternalForm();
		this.url = url;
		setCookieManager(jCurlCookieManager);
	}

	/**
	 * This constructor instantiates the object with the urlAsString that is passed in. The urlAsString is then
	 * transformed into an URL in the GetURL method.
	 * 
	 * @param urlAsString
	 *            The url in string form.
	 */
	public JCurlRequest(String urlAsString) {
		properties = new HashMap<String, String>();
		properties.put("Content-Type", "application/json; charset=utf-8");
		this.urlAsString = urlAsString;
	}

	public JCurlRequest(String urlAsString, JCurlCookieManager jCurlCookieManager) {
		properties = new HashMap<String, String>();
		properties.put("Content-Type", "application/json; charset=utf-8");
		this.urlAsString = urlAsString;
		setCookieManager(jCurlCookieManager);
	}
	
	public void setCookieManager(JCurlCookieManager jCurlCookieManager) {
		if (jCurlCookieManager != null) {
			this.jCurlCookieManager = jCurlCookieManager;
		}
	}
	
	/**
	 * This method fetches appropriate cookies from the cookie manager and stores them as properties.
	 */
	public void updateCookies() {
		if (jCurlCookieManager != null) {
			jCurlCookieManager.updateCookies(this);
		}
	}

	/**
	 * This method returns the properties Map. Wanted properties can be set with normal Map functions.
	 * <p>
	 * JCurlRequest request = new JCurlRequest("http://viaplay.se"); Map<String, String> properties
	 * request.getProperties(); properties.put("Content-Type", "text/plain"); JCurlResponse response =
	 * JCurl.get(request);
	 * </p>
	 * 
	 * @return The properties Map
	 */
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		return properties;
	}

	/**
	 * This method returns the URL if it is already set or constructs one from the urlAsString field.
	 * 
	 * @return The URL object.
	 * @throws MalformedURLException
	 */
	public URL getURL() throws MalformedURLException {
		if (url == null) {
			url = new URL(urlAsString);
		}
		return url;
	}

	/**
	 * The method returns the url in String form regardless of how it was set. I.e via URL or urlAsString.
	 * 
	 * @return The url in String form
	 */
	public String getUrlAsString() {
		return urlAsString;
	}

	/**
	 * This method sets the url in String form. Use this method if you want to reuse the JCurlRequest object.
	 * 
	 * @param urlAsString
	 *            The url in String form
	 */
	public void setUrlAsString(String urlAsString) {
		this.url = null;
		this.urlAsString = urlAsString;
	}

	/**
	 * This method returns the HTTP method that is set on this request object. This method only returns the used method
	 * after the request has been processed.
	 * 
	 * @return The HTTP method in String form.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * This setter method sets the HTTP method of the transmission. JCurl sets this parameter in its methods head(),
	 * get(), put(), post() and delete(). It doesn't give any effect to set it prior to calling JCurl.
	 * 
	 * @param method
	 *            The wanted method in String form.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * This method returns the current time out set in this request. If not set explicitly the default value is 5000
	 * milliseconds.
	 * 
	 * @return The time out value in milliseconds.
	 */
	public int getTimeOutMillis() {
		return timeOutMillis;
	}

	/**
	 * This setter method sets the wanted time out form the transmission. See the JCurlTest class for example of usage.
	 * 
	 * @param timeOutMillis
	 *            The wanted time out in milliseconds.
	 */
	public void setTimeOutMillis(int timeOutMillis) {
		this.timeOutMillis = timeOutMillis;
	}

	/**
	 * Simple pay-load checker that is used by JCurl.
	 * 
	 * @return true if there is a pay-load set or else false.
	 */
	public boolean hasPayload() {
		return (payload != null && !payload.isEmpty());
	}

	/**
	 * This method returns the current pay-load.
	 * 
	 * @return The payload in String form.
	 */
	public String getPayload() {
		return payload;
	}

	/**
	 * This method can be used to set the pay-load of this transmission.
	 * 
	 * @param payload
	 *            The pay-load in String form.
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

	/**
	 * This method returns the current char-set name for this request. Default if UTF-8. The char-set name is used to
	 * convert the received pay-load.
	 * 
	 * @return The name of the set char-set.
	 */
	public String getCharsetName() {
		return charsetName;
	}

	/**
	 * This setter method sets the wanted char-set name for the received pay-load.
	 * 
	 * @param charsetName
	 *            The name of the char-set.
	 */
	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	/**
	 * The checker is used by JCurl to decide if an exceptions is to be thrown in case of an faulty request. It is the
	 * default behavior of JCurl to not throw anything. If the exception control way is expected set this with the
	 * corresponding setter
	 * 
	 * @return true if exceptions is wanted and false otherwise.
	 */
	public boolean isExceptionsToBeThrown() {
		return exceptionsToBeThrown;
	}

	/**
	 * This setter method can be used to alter the default behavior of not throwing exceptions. Set this to true if
	 * exceptions are the wanted behavior. When not using exceptions the result of the transmission can be checked by
	 * the JCurlResponse.getResponseCode() method that returns the appropriate HTTP response code.
	 * 
	 * @param exceptionsToBeThrown
	 *            Set to true to get exceptions and false to read the result in JCurlResponse.getResponseCode().
	 */
	public void setExceptionsToBeThrown(boolean exceptionsToBeThrown) {
		this.exceptionsToBeThrown = exceptionsToBeThrown;
	}

}

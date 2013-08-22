package com.viaplay.jcurl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class collects all data received from the host after a connection has been made.
 * 
 * @author mikael.p.larsson@afconsult.com
 * 
 */
public class JCurlResponse {
	Logger log = LoggerFactory.getLogger(JCurlResponse.class);
	private StringBuffer responseString = null;
	private int responseCode = -9999;
	private String responseMessage = null;
	private Map<String, List<String>> headerFields = null;
	private JCurlRequest requestObject = null;
	private JCurlCookieManager jCurlCookieManager = null;

	public JCurlResponse() {
	}

	public JCurlResponse(JCurlCookieManager jCurlCookieManager) {
		this.jCurlCookieManager = jCurlCookieManager;
	}

	public void setResponseString(String responseString) {
		this.responseString = new StringBuffer(responseString);
	}

	/**
	 * This method updates itself with data recovered from the HttpUrlConnection if that is present.
	 * 
	 * @param urlConnection
	 */
	public void updateFromUrlConnection(URLConnection urlConnection) {
		headerFields = urlConnection.getHeaderFields();
		if (jCurlCookieManager != null) {
			jCurlCookieManager.updateCookies(this);
		}
		if (urlConnection instanceof HttpURLConnection) {
			try {
				responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
				responseMessage = ((HttpURLConnection) urlConnection).getResponseMessage();
			} catch (IOException e) {
				log.error("An {} occurred when fetching data from HttpURLConnection ({}). The message was: {}",
						e.getClass(), requestObject.getUrlAsString(), e.getMessage());
				setResponseCodeAndMessage(408, "Request Timeout");
			}
		} else {
			setResponseCodeAndMessage(200, "OK");
		}
	}

	/**
	 * This getter method returns the corresponding request object. This is injected by JCurl at the start of the
	 * request.
	 * 
	 * @return The corresponding JCurlRequest object.
	 */
	public JCurlRequest getRequestObject() {
		return requestObject;
	}

	/**
	 * This setter method sets the JCurlRequest object to this object. This is mainly used as an injector that JCurl
	 * uses. There is normally no reason for setting this if you are not working with JCurl.
	 * 
	 * @param requestObject
	 *            The corresponding JCurlRequest object.
	 */
	public void setRequestObject(JCurlRequest requestObject) {
		this.requestObject = requestObject;
	}

	/**
	 * This getter returns the response from the request in StringBuffer form. This is convenient if you want to reduce
	 * the number of Strings created or in case of a large response.
	 * 
	 * @return The response in StringBuffer form.
	 */
	public StringBuffer getResponseString() {
		return responseString;
	}

	/**
	 * This setter method is an injector method used by JCurl. There is normally no point in using this setter.
	 * 
	 * @param responseString
	 *            The response in StringBuffer form.
	 */
	public void setResponseString(StringBuffer responseString) {
		this.responseString = responseString;
	}

	/**
	 * This method returns the response code received from the host.
	 * 
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * This setter method sets the value to the responseCode field in this object. It is mainly used by JCurl and
	 * JCurlResponse itself to inject the response code into this object.
	 * 
	 * @param responseCode
	 *            The response code in numeric (int) form.
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * This method returns the response message from the host.
	 * 
	 * @return
	 */
	public String getResponseMessage() {
		return responseMessage;
	}

	/**
	 * This setter method sets the value to the responseMessage field in this object. It is mainly used by JCurl and
	 * JCurlResponse itself to inject the response message into this object.
	 * 
	 * @param responseMessage
	 *            The response message in String form.
	 */
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	/**
	 * This method returns response code and response message concatenated together.
	 * 
	 * @return
	 */
	public String getResponseCodeAndMessage() {
		return String.format("%s %s", responseCode, responseMessage);
	}

	/**
	 * This setter method sets both the value to the responseCode field and the responseMessage field in this object. It
	 * is mainly used by JCurl and JCurlResponse itself to inject the the values into this object.
	 * 
	 * @param responseCode
	 *            The response code in numeric (int) form.
	 * @param responseMessage
	 *            The response message in String form.
	 */
	public void setResponseCodeAndMessage(int responseCode, String responseMessage) {
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
	}

	/**
	 * This method returns the headerFields map that is returned from the host.
	 * 
	 * @return
	 */
	public Map<String, List<String>> getHeaderFields() {
		return headerFields;
	}

	/**
	 * This method returns only the payload received from the host.
	 */
	@Override
	public String toString() {
		return responseString.toString();
	}

}

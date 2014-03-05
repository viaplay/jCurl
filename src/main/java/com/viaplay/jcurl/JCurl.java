package com.viaplay.jcurl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.viaplay.jcurl.exception.JCurlFileNotFoundException;
import com.viaplay.jcurl.exception.JCurlIOException;
import com.viaplay.jcurl.exception.JCurlMalformedURLException;
import com.viaplay.jcurl.exception.JCurlSocketTimeoutException;

/**
 * JCurl is a simple yet powerful resource getter that works very much like the curl command line tool we all have used
 * and learned to love. In its simplest form it fetches data from an entered url and return that data in a form of a
 * String for further processing or direct use.
 * 
 * @author mikael.p.larsson@afconsult.com
 * 
 */
public class JCurl {

	/**
	 * The head request does only fetch the status of an resource without transmitting any pay-load.
	 * 
	 * @param urlAsString
	 *            The url in String form to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse head(String urlAsString) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		return head(request);
	}

	public static JCurlResponse head(String urlAsString, JCurlCookieManager jCurlCookieManager) {
		JCurlRequest request = new JCurlRequest(urlAsString, jCurlCookieManager);
		return head(request);
	}

	/**
	 * The head request does only fetch the status of an resource without transmitting any pay-load.
	 * 
	 * @param request
	 *            A populated JCurlRequest object to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse head(JCurlRequest request) {
		JCurlResponse response = new JCurlResponse();
		request.setMethod(JCurlRequest.HEAD);
		request.setPayload(null);
		doHttpCall(request, response);
		return response;
	}

	public static JCurlResponse head(JCurlRequest request, JCurlCookieManager jCurlCookieManager) {
		request.setCookieManager(jCurlCookieManager);
		JCurlResponse response = new JCurlResponse(jCurlCookieManager);
		request.setMethod(JCurlRequest.HEAD);
		request.setPayload(null);
		doHttpCall(request, response);
		return response;
	}

	/**
	 * The get request is the type of request that a web browser does when it fetches a web page and other resources
	 * like an image.
	 * 
	 * @param urlAsString
	 *            The url in String form to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse get(String urlAsString) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		return get(request);
	}

	public static JCurlResponse get(String urlAsString, JCurlCookieManager jCurlCookieManager) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		return get(request, jCurlCookieManager);
	}

	/**
	 * The get request is the type of request that a web browser does when it fetches a web page and other resources
	 * like an image.
	 * 
	 * @param request
	 *            A populated JCurlRequest object to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse get(JCurlRequest request) {
		JCurlResponse response = new JCurlResponse();
		request.setPayload(null);
		doHttpCall(request, response);
		return response;
	}

	public static JCurlResponse get(JCurlRequest request, JCurlCookieManager jCurlCookieManager) {
		request.setCookieManager(jCurlCookieManager);
		JCurlResponse response = new JCurlResponse(jCurlCookieManager);
		request.setPayload(null);
		doHttpCall(request, response);
		return response;
	}

	/**
	 * The put request is an important request when dealing with RESTful web services.
	 * 
	 * @param urlAsString
	 *            The url in String form to the wanted resource.
	 * @param payload
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse put(String urlAsString, String payload) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		request.setPayload(payload);
		return put(request);
	}

	public static JCurlResponse put(String urlAsString, String payload, JCurlCookieManager jCurlCookieManager) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		request.setPayload(payload);
		return put(request, jCurlCookieManager);
	}

	/**
	 * The put request is an important request when dealing with RESTful web services.
	 * 
	 * @param request
	 *            A populated JCurlRequest object to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse put(JCurlRequest request) {
		JCurlResponse response = new JCurlResponse();
		request.setMethod(JCurlRequest.PUT);
		doHttpCall(request, response);
		return response;
	}

	public static JCurlResponse put(JCurlRequest request, JCurlCookieManager jCurlCookieManager) {
		request.setCookieManager(jCurlCookieManager);
		JCurlResponse response = new JCurlResponse(jCurlCookieManager);
		request.setMethod(JCurlRequest.PUT);
		doHttpCall(request, response);
		return response;
	}

	/**
	 * The post request is the type of request a web server does when a html form is submitted.
	 * 
	 * @param urlAsString
	 *            The url in String form to the wanted resource.
	 * @param payload
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse post(String urlAsString, String payload) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		request.setPayload(payload);
		return post(request);
	}

	public static JCurlResponse post(String urlAsString, String payload, JCurlCookieManager jCurlCookieManager) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		request.setPayload(payload);
		return post(request, jCurlCookieManager);
	}

	/**
	 * The post request is the type of request a web server does when a html form is submitted.
	 * 
	 * @param request
	 *            A populated JCurlRequest object to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse post(JCurlRequest request) {
		JCurlResponse response = new JCurlResponse();
		request.setMethod(JCurlRequest.POST);
		doHttpCall(request, response);
		return response;
	}

	public static JCurlResponse post(JCurlRequest request, JCurlCookieManager jCurlCookieManager) {
		request.setCookieManager(jCurlCookieManager);
		JCurlResponse response = new JCurlResponse(jCurlCookieManager);
		request.setMethod(JCurlRequest.POST);
		doHttpCall(request, response);
		return response;
	}

	/**
	 * The delete request is an important request when dealing with RESTful web services. Beware that this request will
	 * delete an resource if the request is successful.
	 * 
	 * @param urlAsString
	 *            The url in String form to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse delete(String urlAsString) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		return delete(request);
	}

	public static JCurlResponse delete(String urlAsString, JCurlCookieManager jCurlCookieManager) {
		JCurlRequest request = new JCurlRequest(urlAsString);
		return delete(request, jCurlCookieManager);
	}

	/**
	 * The delete request is an important request when dealing with RESTful web services. Beware that this request will
	 * delete an resource if the request is successful.
	 * 
	 * @param request
	 *            A populated JCurlRequest object to the wanted resource.
	 * @return a filled in JCurlResponse object.
	 */
	public static JCurlResponse delete(JCurlRequest request) {
		JCurlResponse response = new JCurlResponse();
		request.setMethod(JCurlRequest.DELETE);
		request.setPayload(null);
		doHttpCall(request, response);
		return response;
	}

	public static JCurlResponse delete(JCurlRequest request, JCurlCookieManager jCurlCookieManager) {
		request.setCookieManager(jCurlCookieManager);
		JCurlResponse response = new JCurlResponse(jCurlCookieManager);
		request.setMethod(JCurlRequest.DELETE);
		request.setPayload(null);
		doHttpCall(request, response);
		return response;
	}

	/**
	 * This method does the actual communication to simplify the methods above.
	 * 
	 * @param request
	 *            A populated JCurlRequest object to the wanted resource.
	 * @param response
	 *            An instantiated JCurlResponse object.
	 */
	private static void doHttpCall(JCurlRequest request, JCurlResponse response) {
		Logger log = LoggerFactory.getLogger(JCurl.class);
		StringBuffer result = new StringBuffer();
		URLConnection urlConnection = null;
		response.setRequestObject(request);
		
		request.updateCookies();

		try {
			urlConnection = (URLConnection) request.getURL().openConnection();
			urlConnection.setDoInput(true);
			if (urlConnection instanceof HttpURLConnection) {
				((HttpURLConnection) urlConnection).setRequestMethod(request.getMethod());
			}
			urlConnection.setConnectTimeout(request.getTimeOutMillis());
			urlConnection.setReadTimeout(request.getTimeOutMillis());

			if (request.getURL().getUserInfo() != null) {
				String basicAuth = "Basic "
						+ new String(new Base64().encode(request.getURL().getUserInfo().getBytes()));
				urlConnection.setRequestProperty("Authorization", basicAuth);
			}
			urlConnection.setRequestProperty("Content-Length", "0");
			for (String key : request.getProperties().keySet()) {
				urlConnection.setRequestProperty(key, request.getProperties().get(key));
			}

			if (request.hasPayload()) {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Length",
						"" + request.getPayload().getBytes(request.getCharsetName()).length);

				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream(),
						request.getCharsetName());
				outputStreamWriter.write(request.getPayload());
				outputStreamWriter.close();
			}

			response.updateFromUrlConnection(urlConnection);

			readInputStream(result, urlConnection.getInputStream(), request.getCharsetName());

		} catch (SocketTimeoutException e) {
			response.setResponseCodeAndMessage(408, "The socket connection timed out.");
			log.error("The socket timed out after {} milliseconds.", request.getTimeOutMillis());
			if (request.isExceptionsToBeThrown())
				throw new JCurlSocketTimeoutException(e);
		} catch (RuntimeException e) {
			response.setResponseCodeAndMessage(500, "Internal server error.");
			log.error(e.getMessage());
			if (request.isExceptionsToBeThrown())
				throw e;
		} catch (MalformedURLException e) {
			response.setResponseCodeAndMessage(400, "The url is malformed.");
			log.error("The url '{}' is malformed.", request.getUrlAsString());
			if (request.isExceptionsToBeThrown())
				throw new JCurlMalformedURLException(e);
		} catch (IOException e) {
			if (urlConnection instanceof HttpURLConnection) {
				try {
					readInputStream(result, ((HttpURLConnection) urlConnection).getErrorStream(),
							request.getCharsetName());
				} catch (IOException e1) {
					if (response.getResponseCode() < 0) {
						response.setResponseCodeAndMessage(500, "Internal server error.");
					}
					if (request.isExceptionsToBeThrown()) {
						if (e instanceof FileNotFoundException) {
							throw new JCurlFileNotFoundException(e);
						} else {
							throw new JCurlIOException(e);
						}
					}
				}
			} else {
				response.setResponseCodeAndMessage(404, "Not Found.");
				if (request.isExceptionsToBeThrown())
					throw new JCurlFileNotFoundException(e);
			}
		} finally {
			if (urlConnection != null && urlConnection instanceof HttpURLConnection) {
				((HttpURLConnection) urlConnection).disconnect();
			}
		}
		response.setResponseString(result);
	}

	/**
	 * Local helper method that reads data from an input stream.
	 * 
	 * @param result
	 *            The read text.
	 * @param inputStream
	 *            The stream to read.
	 * @param charsetName
	 *            The name of the char-set to be used to convert the read pay-load.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private static void readInputStream(StringBuffer result, InputStream inputStream, String charsetName)
			throws UnsupportedEncodingException, IOException {
		if (inputStream == null)
			throw new IOException("No working inputStream.");
		InputStreamReader streamReader = new InputStreamReader(inputStream, charsetName);
		BufferedReader bufferedReader = new BufferedReader(streamReader);

		String row;
		while ((row = bufferedReader.readLine()) != null) {
			result.append(row);
			result.append("\n");
		}

		bufferedReader.close();
		streamReader.close();
	}

}

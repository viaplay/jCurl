package com.viaplay.jcurl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.viaplay.jcurl.exception.JCurlFileNotFoundException;
import com.viaplay.jcurl.exception.JCurlIOException;
import com.viaplay.jcurl.exception.JCurlSocketTimeoutException;

/**
 * This class tests all features of the JCurl class. This is the best spot to gather information about how to use JCurl.
 * 
 * @author mikael.p.larsson@afconsult.com
 * 
 */
public class JCurlTest {
	Logger log = LoggerFactory.getLogger(JCurlTest.class);
	
	
	@Test
	public void testTempCookie() {
		JCurlRequest request = new JCurlRequest("http://viaplay.se", JCurlCookieManager.getInstance());
		JCurlResponse response = JCurl.get(request, JCurlCookieManager.getInstance());
		log.debug(response.toString());
	}

	/**
	 * This test fetch a plain text file from the src/main/resources folder.
	 */
	@Test
	public void testGetLocalFileFromFileURL() {
		URL url = this.getClass().getResource("testfile.json");
		JCurlRequest request = new JCurlRequest(url);
		JCurlResponse response = JCurl.get(request);
		assertContains("item", response);
		assertEquals(200, response.getResponseCode());
	}
	
	/**
	 * This test shows how to use the JCurl to read a HTML page from a server. To make this happen we must have a web
	 * server to speak to thus that is created first and destroyed afterwards. Between the special commented lines the
	 * actual code resides.
	 */
	@Test
	public void testGetHTMLFromMicroHttpServer() {
		MicroHTTPServer server = new MicroHTTPServer(1962);
		server.startServer();
		// ----------------------------------------------------------------

		JCurlResponse response = JCurl.get("http://localhost:1962/");
		log(response);

		// ----------------------------------------------------------------
		server.stopServer();
	}

	/**
	 * This test shows how to use the JCurl to read a HTML page from a server. To make this happen we must have a web
	 * server to speak to thus that is created first and destroyed afterwards. Between the special commented lines the
	 * actual code resides.
	 */
	@Test
	public void testGetHTMLFromMicroHttpServerUsingCookieManager() {
		MicroHTTPServer server = new MicroHTTPServer(1961);
		server.startServer();
		// ----------------------------------------------------------------

		JCurlResponse response = JCurl.get("http://localhost:1961/", JCurlCookieManager.getInstance());
		assertMissing("Cookie=", response.getRequestObject().getProperties().toString());
		assertMissing("NotYourCookie", response.getRequestObject().getProperties().toString());
		
		response = JCurl.get("http://localhost:1961/", JCurlCookieManager.getInstance());
		assertContains("SimpleCookie", response.getRequestObject().getProperties().toString());
		assertContains("YourCookie", response.getRequestObject().getProperties().toString());
		assertContains("NotExpiredCookie", response.getRequestObject().getProperties().toString());
		assertMissing("OutdatedCookie", response.getRequestObject().getProperties().toString());
		assertMissing("YourCookieTestCookie", response.getRequestObject().getProperties().toString());
		
		response = JCurl.get("http://localhost:1961/cookietest/index.html", JCurlCookieManager.getInstance());
		assertMissing("OutdatedCookie", response.getRequestObject().getProperties().toString());
		assertContains("YourCookieTestCookie", response.getRequestObject().getProperties().toString());

		// ----------------------------------------------------------------
		server.stopServer();
	}

	/**
	 * Here is a sample of how you can use JCurl to create a CouchDB database and fill it with data. To be reusable it
	 * also checks if the database exists in the beginning and deletes it if it does.
	 */
	@Test
	@Ignore
	public void testFillCouchDBDatabase() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss-SSS");
		String databaseUrl = "http://username:password@couchdb:5984/jcurltestdb";

		/* Check if the database exists and delete if so */
		JCurlResponse response = JCurl.head(databaseUrl);
		if (response.getResponseCode() == 200) {
			JCurl.delete(databaseUrl);
		}
		/* Create the database */
		response = JCurl.put(databaseUrl, null);

		/* Fill the database */
		for (int i = 0; i < 100; i++) {
			Date now = new Date();
			String dateString = dateFormat.format(now);
			response = JCurl.put(String.format("%s/%s", databaseUrl, dateString),
					String.format("{\"time\":\"%s\", \"no\":%s}", dateString, i));
		}
	}

	/**
	 * This test fetch the resource file using urlAsString method and verifies the response code.
	 */
	@Test
	public void testGetLocalFileViaUrlAsString() {
		URL url = this.getClass().getResource("testfile.json");
		String urlAsString = url.toExternalForm();
		JCurlResponse response = JCurl.get(urlAsString);
		assertContains("item", response);
		assertEquals(200, response.getResponseCode());
	}

	/**
	 * This test fetch the file using urlAsString method and verifies the response code.
	 */
	@Test
	public void testGetMissingLocalFileViaUrlAsString() {
		JCurlResponse response = JCurl.get("file:/this.file.does.not.exist");
		assertEquals("", response.toString());
		assertEquals(404, response.getResponseCode());
	}

	/**
	 * This test fetch the missing file using urlAsString method and verifies that an exception is thrown.
	 */
	@Test
	public void testGetMissingLocalFileViaUrlAsStringWithExceptions() {
		JCurlRequest request = new JCurlRequest("file:/this.file.does.not.exist");
		request.setExceptionsToBeThrown(true);
		JCurlResponse response = null;
		try {
			response = JCurl.get(request);
		} catch (JCurlFileNotFoundException e) {
			assertEquals(null, response);
			return;
		}
		fail("You should not come this far");
	}

	/**
	 * This test verifies all method using the built in MicroHTTPServer.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAllUsingMicroHttpServer() throws IOException, InterruptedException {
		MicroHTTPServer server = new MicroHTTPServer();
		server.startServer();

		JCurlResponse response = JCurl.head("http://localhost:1989/");
		assertHTTP(response, 200);
		log(response);
		response = JCurl.get("http://localhost:1989/");
		assertHTTP(response, 200);
		log(response);
		response = JCurl.put("http://localhost:1989/put", "payload");
		assertHTTP(response, 201);
		log(response);
		response = JCurl.post("http://localhost:1989/post", "payload");
		assertHTTP(response, 201);
		log(response);
		response = JCurl.delete("http://localhost:1989/delete");
		assertHTTP(response, 200);
		log(response);

		response = JCurl.head("http://localhost:1989/old");
		assertHTTP(response, 304);
		log(response);
		response = JCurl.head("http://localhost:1989/bad");
		assertHTTP(response, 400);
		log(response);
		response = JCurl.head("http://localhost:1989/authenticate");
		assertHTTP(response, 403);
		log(response);
		response = JCurl.head("http://localhost:1989/missing");
		assertHTTP(response, 404);
		log(response);
		response = JCurl.head("http://localhost:1989/error");
		assertHTTP(response, 500);
		log(response);

		response = JCurl.get("http://localhost:1989/old");
		assertHTTP(response, 304);
		log(response);
		response = JCurl.get("http://localhost:1989/bad");
		assertHTTP(response, 400);
		log(response);
		response = JCurl.get("http://localhost:1989/authenticate");
		assertHTTP(response, 403);
		log(response);
		response = JCurl.get("http://localhost:1989/missing");
		assertHTTP(response, 404);
		log(response);
		response = JCurl.get("http://localhost:1989/error");
		assertHTTP(response, 500);
		log(response);

		response = JCurl.put("http://localhost:1989/old", "payload");
		assertHTTP(response, 304);
		log(response);
		response = JCurl.put("http://localhost:1989/bad", "payload");
		assertHTTP(response, 400);
		log(response);
		response = JCurl.put("http://localhost:1989/authenticate", "payload");
		assertHTTP(response, 403);
		log(response);
		response = JCurl.put("http://localhost:1989/missing", "payload");
		assertHTTP(response, 404);
		log(response);
		response = JCurl.put("http://localhost:1989/error", "payload");
		assertHTTP(response, 500);
		log(response);

		response = JCurl.post("http://localhost:1989/old", "payload");
		assertHTTP(response, 304);
		log(response);
		response = JCurl.post("http://localhost:1989/bad", "payload");
		assertHTTP(response, 400);
		log(response);
		response = JCurl.post("http://localhost:1989/authenticate", "payload");
		assertHTTP(response, 403);
		log(response);
		response = JCurl.post("http://localhost:1989/missing", "payload");
		assertHTTP(response, 404);
		assertContains("404", response);
		log(response);
		response = JCurl.post("http://localhost:1989/error", "payload");
		assertHTTP(response, 500);
		log(response);

		server.stopServer();

		JCurlRequest request = new JCurlRequest("http://localhost:1989/");
		request.setTimeOutMillis(100);
		response = JCurl.get(request);
		assertHTTP(response, 408);
		log(response);

		log.info("Test has ended!");
	}

	/**
	 * This test verifies all method exception throwing using the built in MicroHTTPServer.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testAllExceptionsUsingMicroHttpServer() throws IOException, InterruptedException {
		int port = 1991;
		String urlFormat = "http://localhost:%s%s";
		MicroHTTPServer server = new MicroHTTPServer(port);
		server.startServer();
		JCurlRequest request = new JCurlRequest("");
		request.setExceptionsToBeThrown(true);

		try {
			request.setUrlAsString(String.format(urlFormat, port, "/bad"));
			JCurl.head(request);
			fail("Expecting JCurlIOException to be thrown.");
		} catch (JCurlIOException e) {
			assertContains("400", e.getMessage());
			assertContains("IOException", e.getMessage());
		}
		try {
			request.setUrlAsString(String.format(urlFormat, port, "/authenticate"));
			JCurl.head(request);
			fail("Expecting JCurlIOException to be thrown.");
		} catch (Exception e) {
			assertContains("403", e.getMessage());
			assertContains(".IOException", e.getMessage());
		}
		try {
			request.setUrlAsString(String.format(urlFormat, port, "/missing"));
			JCurl.head(request);
			fail("Expecting JCurlFileNotFoundException to be thrown.");
		} catch (JCurlFileNotFoundException e) {
			assertContains(".FileNotFoundException", e.getMessage());
		}
		try {
			request.setUrlAsString(String.format(urlFormat, port, "/error"));
			JCurl.head(request);
			fail("Expecting JCurlIOException to be thrown.");
		} catch (JCurlIOException e) {
			assertContains("500", e.getMessage());
			assertContains(".IOException", e.getMessage());
		}

		server.stopServer();

		try {
			request.setTimeOutMillis(200);
			request.setUrlAsString(String.format(urlFormat, port, "/"));
			JCurl.head(request);
			fail("Expecting JCurlIOException to be thrown.");
		} catch (JCurlSocketTimeoutException e) {
			assertContains(".SocketTimeoutException", e.getMessage());
		}
		log.info("Test has ended!");
	}
	
	@Test
	public void testCouchDBPutPostGetDeleteTest() {
		JCurlResponse response;
		String JavaVMServerUrl = System.getProperty("serverUrl");
		String serverUrl = JavaVMServerUrl!=null ? JavaVMServerUrl : "http://username:password@localhost:5984";
		String databaseName = "/unique_jcurltestdb_should_not_remain";
		response = JCurl.put(serverUrl+databaseName, null);
		if (response.getResponseCode() == 201) {
			try {
				JCurl.post(serverUrl+databaseName, "{\"tjosan\": \"hejsan\"}");
				String payLoad = JCurl.get(serverUrl+databaseName + "/_all_docs?include_docs=true").toString();
				assertContains("total_rows\":1", payLoad);
				assertContains("tjosan", payLoad);
				assertContains("hejsan", payLoad);
				JCurl.put(serverUrl+databaseName + "/tjena", "{\"tjosan\": \"hejsan\"}");
				payLoad = JCurl.get(serverUrl+databaseName + "/tjena").toString();
				assertContains("tjena", payLoad);
				assertContains("tjosan", payLoad);
				assertContains("hejsan", payLoad);
			} finally {
				JCurl.delete(serverUrl+databaseName);
			}
		} else {
			log.error("The database {} may already exist at {}. Error and code: {}", databaseName, serverUrl, response.getResponseCodeAndMessage());
		}
	}
	
	@Test
	public void testDeleteWithPayload() {
		JCurlRequest request = new JCurlRequest("http://viadevice.viaplay.tv/api/devices/deviceId/0123923323423244"); 
		request.setPayload("token=a24ae27cd619082e9fed81770d31e337b867bb37ac348e01fbf8e853cc4fd83b&userId=66CDBA8DA916D518");
		request.getProperties().clear();
		request.getProperties().put("Content-Type", "x-www-form-urlencoded");
		JCurlResponse response = JCurl.delete(request);
		System.out.format("The response is '%s'", response.toString());
	}


	/* Helper methods below this line */

	private void assertHTTP(JCurlResponse response, int responseCode) {
		assertEquals(responseCode, response.getResponseCode());
	}

	private void log(JCurlResponse response) {
		log.info("The server responded '{}' and returned this data: {} and these header fields: {}",
				response.getResponseCodeAndMessage(), response, response.getHeaderFields());
		log.info("------------------------------------------------------------------------------");
	}

	private void assertContains(String expected, Object was) {
		String testWas = (was instanceof String) ? (String) was : was.toString();
		assertTrue("The expected " + expected + " string is not part of " + testWas, testWas.indexOf(expected) != -1);
	}

	private void assertMissing(String expected, Object was) {
		String testWas = (was instanceof String) ? (String) was : was.toString();
		assertFalse("The expected " + expected + " string is part of " + testWas, testWas.indexOf(expected) != -1);
	}

}

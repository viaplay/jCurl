package com.viaplay.jcurl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a very simple web server that can listen at any free port for HTTP requests. It is no full fledged and is not
 * intended to be used as a web server serving data. The sole purpose of this class is to be able to test web services
 * without the need of a web server, application server, web container and that complex stuff. This is plain Java JRE
 * stuff!
 * <p>
 * In the source of this class you will find the possible 'pages' that you can request. 
 * You can also instantiate and start the server and use your browser to go to the default page http://localhost:1989 
 * and there read about the available pages. There it is also possible to click on each page and see the response.
 * <p>
 * Each page are delivered with a set of cookies so that can be tested as well. These are: 
 * <ul>
 * <li><b>SimpleCookie</b> with value SimpleCookieValue</li>
 * <li><b>NotExpiredCookie</b> with value NotExpiredCookieValue and the expires date set to tomorrow at current time.</li>
 * <li><b>OutdatedCookie</b> with value OutdatedCookieValue and the expires date set to yesterday at current time.</li>
 * <li><b>YourCookie</b> with value YourCookieValue with Path set to / and Domain set to <code>localhost</code>.</li>
 * <li><b>YourCookieTestCookie</b> with value YourCookieTestCookieValue with Path set to /cookietest/ and Domain set to <code>localhost</code>.</li>
 * <li><b>NotYourCookie</b> with value NotYourCookieValue with Path set to / and Domain set to <code>.yourdomain.not</code>.</li>
 * </ul>
 * 
 * @author mikael.p.larsson@afconsult.com
 * 
 */
public class MicroHTTPServer implements Runnable {
	private Logger log = LoggerFactory.getLogger(MicroHTTPServer.class);
	private static final String version = "1.0";

	private Map<String, String> pages = new HashMap<String, String>();
	private static final Map<Integer, String> responseCodes = new HashMap<Integer, String>();

	static {
		responseCodes.put(200, "OK");
		responseCodes.put(201, "Created");
		responseCodes.put(304, "Not Modified");
		responseCodes.put(400, "Bad Request");
		responseCodes.put(403, "Forbidden");
		responseCodes.put(404, "Not Found");
		responseCodes.put(500, "Internal Server Error");
	}

	private int port = 1989;
	private int timeoutMillis = 60000; // The server will be active for max timeoutMillis milliseconds.
	private boolean serverStillUp = true;
	private ServerSocket serverSocket = null;
	private Thread serverThread = null;
	
	private Map<String, String> initializePages() {
		Map<String, String> pages = new HashMap<String, String>();
		pages.put(
				"",
				"200|text/html|<html><head><style>a{display:block;width:150px;}</style></head><body><h1>It works!</h1><p>You have succeeded to connect to MicroHTTPServer!</p><p>Congratulations!</p>"
						+ "<h3>These are the pages that can be used:</h3>"
						+ "<ul>"
						+ "<li><a href=\"/cookietest/index.html\">/cookietest/index.html</a><i>Returns 200 Created and your cookie with path /cookietest/.</i></li>"
						+ "<li><a href=\"create\">create</a><i>Returns 201 Created</i></li>"
						+ "<li><a href=\"delete\">delete</a><i>Returns 200 OK</i></li>"
						+ "<li><a href=\"put\">put</a><i>Returns 201 Created</i></li>"
						+ "<li><a href=\"post\">post</a><i>Returns 201 Created</i></li>"
						+ "<li><a href=\"head\">head</a><i>Returns 200 OK</i></li>"
						+ "<li><a href=\"old\">old</a><i>Returns 304 Not Modified</i></li>"
						+ "<li><a href=\"bad\">bad</a><i>Returns 400 Bad Request</i></li>"
						+ "<li><a href=\"authenticate\">authenticate</a><i>Returns 403 Forbidden</i></li>"
						+ "<li><a href=\"missing\">missing</a><i>Returns 404 Not Found</i></li>"
						+ "<li><a href=\"error\">error</a><i>Returns 500 Internal Server Error</i></li>" + "</ul>");
		pages.put("index.html",
				"200|text/html|<h1>It works!</h1><p>You have succeeded to connect to MicroHTTPServer!</p><p>Congratulations!</p>");
		pages.put("cookietest/index.html",
				"200|text/html|<h1>It works!</h1><p>You have succeeded to connect to the MicroHTTPServer cookietest page!</p><p>Congratulations!</p>");
		pages.put("create", "201|application/json|{\"ok\":true}");
		pages.put("delete", "200|application/json|{\"ok\":true}");
		pages.put("put", "201|application/json|{\"ok\":true}");
		pages.put("post", "201|application/json|{\"ok\":true}");
		pages.put("head", "200|application/json|{\"ok\":true}");
		pages.put("old", "304|text/html|<h1>Same old, same old</h1>");
		pages.put("bad", "400|text/plain;charset=utf-8|");
		pages.put("authenticate", "403|text/plain;charset=utf-8|");
		pages.put("missing",
				"404|text/html|<h1>404 Not Found</h1><p>The page you are requesting cannot be found!</p><p>Please try again!</p>");
		pages.put("error", "500|text/plain;charset=utf-8|");
		
		return pages;
	}

	public MicroHTTPServer() {
		pages = initializePages();
	}

	/**
	 * Use this constructor in need to change to port.
	 * 
	 * @param port
	 */
	public MicroHTTPServer(int port) {
		this();
		this.port = port;
	}

	/**
	 * This parameter can be set to a time out time when the web server should be closed.
	 * 
	 * @param timeOutMillis
	 */
	public void setTimeoutMillis(int timeOutMillis) {
		this.timeoutMillis = timeOutMillis;
	}
	
	public Map<String, String> getPagesMap() {
		return pages;
	}

	/**
	 * This method starts this server in a Thread of its own.
	 */
	public void startServer() {
		serverThread = new Thread(this, "MicroHTTPServer");
		serverThread.start();
		try {
			Thread.sleep(150L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method makes the server stop responding.
	 */
	public void stopServer() {
		serverStillUp = false;
		try {
			Thread.sleep(150L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method fulfills the interface runnable and makes this class runnable. :P
	 */
	public void run() {
		try {
			listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Here is where the action takes part.
	 * 
	 * @throws IOException
	 */
	public void listen() throws IOException {
		String request = null;
		String method = null;
		String urlAsString = null;
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(100);
		log.info("The {} is active and monitors port {} {}.", this.getClass().getName(), port,
				timeoutMillis == 0 ? "indefinitely" : String.format("for %s milliseconds", timeoutMillis));

		while (serverStillUp) {
			StringBuffer body = new StringBuffer();
			Map<String, String> headerMap = new HashMap<String, String>();
			long endBy = System.currentTimeMillis() + timeoutMillis;

			try {
				Socket clientSocket = serverSocket.accept();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
						clientSocket.getOutputStream()));

				request = readRequest(body, bufferedReader, headerMap);
				if (request != null) {
					String[] requestArr = request.split(" ");
					method = requestArr[0];
					urlAsString = requestArr[1];
					log.info("A {} request has been received for {} from {}:{}", method, urlAsString, clientSocket
							.getInetAddress().getHostAddress(), clientSocket.getPort());

					log.debug("The header strings are {}", headerMap);
					log.debug("Body content({}): {}", body.length(), body);

					writeResponse(urlAsString, method, bufferedWriter);
				} 

				bufferedWriter.close();
				bufferedReader.close();
				clientSocket.close();

			} catch (SocketTimeoutException e) {
				/*
				 * This exception is normally quite OK thus no particular action here. However, we keep track so that
				 * the server ends after the timeout if it is set apart from zero.
				 */
				if (timeoutMillis != 0 && System.currentTimeMillis() > endBy) {
					break;
				}
			}
		}
		log.info("The server is closed!");
	}

	/**
	 * This method read all characters in the request and separates them into different parts.
	 * @param body The body part of the request
	 * @param in The buffered reader
	 * @param headerMap The map where the header parameters are placed.
	 * @return The url part of the request.
	 * @throws IOException In case of IO matters.
	 */
	private String readRequest(StringBuffer body, BufferedReader in, Map<String, String> headerMap) throws IOException {

		final int REQUEST = 0;
		final int HEADERS = 1;
		final int BODY = 2;

		String request = null;
		String row = null;

		int mode = REQUEST;
		while (((row = in.readLine()) != null)) {
			if (row.isEmpty()) {
				int contentLength;
				try {
					contentLength = Integer.parseInt(headerMap.get("Content-Length"));
				} catch (NumberFormatException e) {
					contentLength = 0;
				}
				char[] cbuf = new char[contentLength];
				in.read(cbuf);
				body.append(cbuf);
				break;
			}
			switch (mode) {
			case REQUEST:
				request = row;
				mode++;
				break;
			case HEADERS:
				String[] splitRow = row.split(" ");
				String key = splitRow[0];
				String value = row.substring(key.length() + 1);
				headerMap.put(key.substring(0, key.length() - 1), value);
				break;
			case BODY:
				body.append(row);
			}
		}
		return request;
	}

	/**
	 * This method sends the correct response based on the url.
	 * @param urlAsString The requested url.
	 * @param out The writer to write the page to.
	 * @throws IOException In case of something goes wrong.
	 */
	private void writeResponse(String urlAsString, String method, BufferedWriter out) throws IOException {
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.roll(Calendar.DAY_OF_YEAR, true);
		Calendar yesterday = Calendar.getInstance();
		yesterday.roll(Calendar.DAY_OF_YEAR, false);
		
		String pageData = pages.get(urlAsString.substring(1));
		if (pageData == null) {
			pageData = pages.get("missing");
		}
		log.debug("pageData is {}", pageData);
		String[] pageDataArr = pageData.split("\\|");
		int responseCode = Integer.parseInt(pageDataArr[0]);
		String contentType = pageDataArr.length >= 2 ? pageDataArr[1] : "";
		String page = pageDataArr.length == 3 ? pageDataArr[2] : "";

		out.write(String.format("HTTP/1.1 %s %s\r\n", responseCode, responseCodes.get(responseCode)));
		out.write(String.format("Server: %s/%s\r\n", this.getClass().getSimpleName(), version));
		out.write(String.format("Content-Type: %s\r\n", contentType));
		out.write(String.format("Content-Length: %s\r\n", page.length()));
		out.write(String.format("Set-Cookie: %s\r\n", "SimpleCookie=SimpleCookieValue"));
		out.write(String.format("Set-Cookie: NotExpiredCookie=NotExpiredCookieValue; Expires=%s; \r\n", DateUtil.formatDate(tomorrow.getTime())));
		out.write(String.format("Set-Cookie: OutdatedCookie=OutdatedCookieValue; Expires=%s; \r\n", DateUtil.formatDate(yesterday.getTime())));
		out.write(String.format("Set-Cookie: YourCookie=YourCookieValue; Path=%s; Domain=%s;\r\n", "/", "localhost"));
		out.write(String.format("Set-Cookie: YourCookieTestCookie=YourCookieTestCookieValue; Path=%s; Domain=%s;\r\n", "/cookietest/", "localhost"));
		out.write(String.format("Set-Cookie: NotYourCookie=NotYourCookieValue; Path=/; Domain=.yourdomain.not;\r\n"));
		out.write("\r\n");
		if (!"HEAD".equals(method)) {
			out.write(page);
			out.write("\r\n");
			out.write("\r\n");
		}

		out.flush();

		log.info("The client request is processed.");

	}

	/**
	 * This main function can be used to test the server.
	 * @param args As you know them - not used.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		MicroHTTPServer server = new MicroHTTPServer(1989);
		server.timeoutMillis = 0;
		server.startServer();
	}

}

package com.viaplay.jcurl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JCurlCookieManager is a singleton object that manages all cookies during a session. By passing this object to the
 * response object as well as the JCurl request methods you ensure that cookies sent from a web server is reused in
 * consecutive requests thus keeping any sessions set up on the server.
 * 
 * @author mikael.p.larsson@afconsult.com
 * 
 */
public class JCurlCookieManager {
	Logger log = LoggerFactory.getLogger(JCurlCookieManager.class);
	
	private static final String SET_COOKIE = "Set-Cookie";

	private static final String cookieExpire = "Expires";
	private static final String cookieDomain = "Domain";
	private static final String cookiePath = "Path";
	private static final String cookieSecure = "Secure";

	private static JCurlCookieManager jCurlCookieManagerInstance = null;
	private Map<String, Cookie> cookieMap = null;

	/**
	 * This method assures that only one instance of this manager is created.
	 * 
	 * @return The one and only JCurlCookieManager object.
	 */
	public static JCurlCookieManager getInstance() {
		if (jCurlCookieManagerInstance == null) {
			jCurlCookieManagerInstance = new JCurlCookieManager();
		}
		return jCurlCookieManagerInstance;
	}

	/**
	 * Lazy constructor of the Map that holds the cookies.
	 * 
	 * @return The instantiated Map object.
	 */
	public Map<String, Cookie> getCookieMap() {
		if (cookieMap == null) {
			cookieMap = new HashMap<String, Cookie>();
		}
		return cookieMap;
	}

	/**
	 * Updates the JCurlCookieManager with cookies found in the header of the response.
	 * 
	 * @param jCurlResponse
	 *            The response object with header data.
	 */
	public void updateCookies(JCurlResponse jCurlResponse) {
		Set<String> keys = jCurlResponse.getHeaderFields().keySet();
		for (String key : keys) {
			if (SET_COOKIE.equals(key)) {
				List<String> stringList = jCurlResponse.getHeaderFields().get(key);
				for (String string : stringList) {
					Cookie cookie = createCookie(string);
					getCookieMap().put(cookie.getName(), cookie);
				}
			}
		}
	}

	/**
	 * This helper method takes the header string of the Set-Cookie header property.
	 * 
	 * @param cookieString
	 *            The value of the Set-Cookie header property.
	 * @return a cookie.
	 */
	private Cookie createCookie(String cookieString) {
		Cookie cookie = null;
		String[] cookieValueArray = cookieString.split(";");
		String name = null;
		String value = null;
		String domain = null;
		String cookieName = "";
		String cookieValue = "";
		Date expires = null;
		String path = null;
		boolean secure = false;
		for (int i = 0; i < cookieValueArray.length; i++) {
			String nameValueString = cookieValueArray[i];
			String[] nameValue = nameValueString.split("=");
			name = nameValue[0].trim();
			value = nameValue.length > 1 ? nameValue[1].trim() : null;
			if (i == 0) {
				cookieName = name;
				cookieValue = value;
			} else {
				if (cookieDomain.equalsIgnoreCase(name)) {
					domain = "".equals(value) ? null : value;
				}
				if (cookiePath.equalsIgnoreCase(name)) {
					path = "".equals(value) ? null : value;
				}
				if (cookieExpire.equalsIgnoreCase(name)) {
					try {
						expires = DateUtil.parseDate(value);
					} catch (DateParseException e) {
						log.warn("The cookie expires value [{}] was not parsable.", value);
					}
				}
				if (cookieSecure.equalsIgnoreCase(name)) {
					secure = true;
				}
			}
		}
		cookie = new Cookie(domain, cookieName, cookieValue, path, expires, secure);
		return cookie;
	}

	/**
	 * Updates the Cookie property in the JCurlRequest object that is passed as a parameter.
	 * 
	 * @param jCurlRequest
	 *            The object that needs an update.
	 */
	public void updateCookies(JCurlRequest jCurlRequest) {
		String cookies = "";
		for (String key : getCookieMap().keySet()) {
			Cookie cookie = getCookieMap().get(key);
			if (!cookie.isExpired()) {
				if (cookie.getDomain() != null) {
					try {
						URL url = jCurlRequest.getURL();
						String host = url.getHost();
						String path = url.getPath();
						if (!host.endsWith(cookie.getDomain()) || !path.startsWith(cookie.getPath())) {
							continue;
						}
					} catch (MalformedURLException e) {
						continue;
					}
				}
				cookies += cookies.length() == 0 ? cookie.toExternalForm() : "; " + cookie.toExternalForm();
			}
		}
		if (!"".equals(cookies)) {
			jCurlRequest.getProperties().put("Cookie", cookies);
		}
	}

}

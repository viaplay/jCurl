package com.viaplay.jcurl;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

	private static Map<String, JCurlCookieManager> jCurlCookieManagerInstanceMap = null;
	private Map<String, JCurlCookie> cookieMap = null;

	/**
	 * This instance getter returns the default cookie manager.
	 * 
	 * @return The default cookie manager.
	 */
	public static JCurlCookieManager getInstance() {
		return getInstance("xzre34_sIngLetOn");
	}

	/**
	 * This method assures that the same instance of this manager is created and then reused.
	 * 
	 * @return The JCurlCookieManager object that match the instanceId.
	 */
	public static JCurlCookieManager getInstance(String instanceId) {
		if (jCurlCookieManagerInstanceMap == null) {
			jCurlCookieManagerInstanceMap = Collections.synchronizedMap(new HashMap<String, JCurlCookieManager>());
		}
		JCurlCookieManager jCurlCookieManagerInstance = jCurlCookieManagerInstanceMap.get(instanceId);
		if (jCurlCookieManagerInstance == null) {
			jCurlCookieManagerInstance = new JCurlCookieManager();
			jCurlCookieManagerInstanceMap.put(instanceId, jCurlCookieManagerInstance);
		}
		return jCurlCookieManagerInstance;
	}

	/**
	 * Lazy constructor of the Map that holds the cookies.
	 * 
	 * @return The instantiated Map object.
	 */
	public Map<String, JCurlCookie> getCookieMap() {
		if (cookieMap == null) {
			cookieMap = new HashMap<String, JCurlCookie>();
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
					JCurlCookie cookie = createCookie(string);
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
	private JCurlCookie createCookie(String cookieString) {
		JCurlCookie cookie = null;
		String[] cookieValueArray = cookieString.split(";");
		String name = null;
		String value = null;
		String domain = null;
		String cookieName = "";
		String cookieValue = "";
		Date expires = null;
		String path = null;
        boolean http = false;
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
                        expires = JCurlCookie.parseDate(value);
                    } catch (ParseException e) {
						log.warn("The cookie expires value [{}] was not parsable.", value);
                    }
				}
				if (cookieSecure.equalsIgnoreCase(name)) {
					secure = true;
				}
			}
		}
		cookie = new JCurlCookie(domain, cookieName, cookieValue, path, expires, http, secure);
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
			JCurlCookie cookie = getCookieMap().get(key);
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

	/**
	 * Reset the contents of this cookie manager.
	 */
	public void reset() {
		cookieMap = null;
	}

}

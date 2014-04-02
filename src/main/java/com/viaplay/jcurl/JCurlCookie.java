package com.viaplay.jcurl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Simple cookie object created to reduce external dependencies.
 * Author: mikael.p.larsson@afconsult.com
 * Date: 2014-04-02
 * Time: 09:29
 */
public class JCurlCookie {
    private static final Logger log = LoggerFactory.getLogger(JCurlCookie.class);
    private static final String EXTERNAL_FORMAT = "[name=%s, domain=%s, path=%s, expiryDate=%s, http=%s, secure=%s, value=%s]";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    private String name;

    private String value;

    private String comment;

    private String domain;

    private Date expiryDate;

    private String path;

    private boolean isSecure;

    private boolean isHttp;

    private boolean hasPathAttribute = false;

    private boolean hasDomainAttribute = false;

    private int version = 0;




    public JCurlCookie() {
        this(null, "noname", null, null, null, false, false);
    }

    public JCurlCookie(String domain, String name, String value) {
        this(domain, name, value, null, null, false, false);
    }

    public JCurlCookie(String domain, String name, String value,
                  String path, Date expires, boolean http, boolean secure) {

        log.trace("enter Cookie(String, String, String, String, Date, boolean)");
        if (name == null) {
            throw new IllegalArgumentException("Cookie name may not be null");
        }
        if (name.trim().equals("")) {
            throw new IllegalArgumentException("Cookie name may not be blank");
        }
        this.setName(name);
        this.setValue(value);
        this.setPath(path);
        this.setDomain(domain);
        this.setExpiryDate(expires);
        this.setHttp(http);
        this.setSecure(secure);
    }

    public JCurlCookie(String domain, String name, String value, String path,
                  int maxAge, boolean http, boolean secure) {

        this(domain, name, value, path, null, http, secure);
        if (maxAge < -1) {
            throw new IllegalArgumentException("Invalid max age:  " + Integer.toString(maxAge));
        }
        if (maxAge >= 0) {
            setExpiryDate(new Date(System.currentTimeMillis() + maxAge * 1000L));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate (Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    static public String formatDate(Date date) {
        if (date == null) return null;
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        return DATE_FORMAT.format(date);

    }

    static public Date parseDate(String dateAsGMTString) throws ParseException {
        return DATE_FORMAT.parse(dateAsGMTString);
    }

    public boolean isPersistent() {
        return (null != expiryDate);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        if (domain != null) {
            int ndx = domain.indexOf(":");
            if (ndx != -1) {
                domain = domain.substring(0, ndx);
            }
            this.domain = domain.toLowerCase();
        }
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getHttp() {
        return isHttp;
    }

    public void setHttp(boolean isHttp) {
        this.isHttp = isHttp;
    }

    public boolean getSecure() {
        return isSecure;
    }

    public void setSecure (boolean secure) {
        isSecure = secure;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isExpired() {
        return (expiryDate != null
                && expiryDate.getTime() <= System.currentTimeMillis());
    }

    public boolean isExpired(Date now) {
        return (expiryDate != null
                && expiryDate.getTime() <= now.getTime());
    }


    public void setPathAttributeSpecified(boolean value) {
        hasPathAttribute = value;
    }

    public boolean isPathAttributeSpecified() {
        return hasPathAttribute;
    }

    public void setDomainAttributeSpecified(boolean value) {
        hasDomainAttribute = value;
    }

    public boolean isDomainAttributeSpecified() {
        return hasDomainAttribute;
    }

    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.getName().hashCode();
        hash = 7 * hash + this.domain.hashCode();
        hash = 7 * hash + this.path.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof JCurlCookie) {
            JCurlCookie that = (JCurlCookie) obj;
            return (this.name.equals(that.name)
                    && this.domain.equals(that.domain)
                    && this.path.equals(that.path));
        } else {
            return false;
        }
    }

    public String toExternalForm() {
        return String.format(EXTERNAL_FORMAT, name, domain, path, JCurlCookie.formatDate(expiryDate), isHttp, isSecure, value);
    }

    public int compare(Object o1, Object o2) {
        log.trace("enter Cookie.compare(Object, Object)");

        if (!(o1 instanceof JCurlCookie)) {
            throw new ClassCastException(o1.getClass().getName());
        }
        if (!(o2 instanceof JCurlCookie)) {
            throw new ClassCastException(o2.getClass().getName());
        }
        JCurlCookie c1 = (JCurlCookie) o1;
        JCurlCookie c2 = (JCurlCookie) o2;
        if (c1.getPath() == null && c2.getPath() == null) {
            return 0;
        } else if (c1.getPath() == null) {
            if (c2.getPath().equals("/")) {
                return 0;
            } else {
                return -1;
            }
        } else if (c2.getPath() == null) {
            if (c1.getPath().equals("/")) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return c1.getPath().compareTo(c2.getPath());
        }
    }

    public String toString() {
        return toExternalForm();
    }


}





# Welcome to Viaplay jCurl project

JCurl is a simple yet powerful resource getter that works very much like the curl command line tool we all have used
and learned to love. In its simplest form it fetches data from an entered url and return that data in a form of a
String for further processing or direct use.



## Why static methods?

All parameters that conducts the behavior of jCurl fits in the JCurlRequest object that is passed into the jCurl request 
functions. Therefore this approach prevents creating an unnecessary object. 



## How to use it?

As always the best way to learn is to read and use the JUnit test classes, in this case the JCurlTest.java.


### A get request in its simplest form

To get a HTML resource from the Internet is a very easy task:

	String htmlCode = JCurl.get("http://localhost:1962/").toString;


### When communicating with a REST web service

Here is a sample of how you can use JCurl to create a CouchDB database and fill it with data. 
To be reusable it also checks if the database exists in the beginning and deletes it if it does.

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


### Take advantage of server cookies in subsequent requests

The latest addition to the JCurl project is the JCurlCookieManager which retrieves all cookies from the responses and adds the valid ones to the request.

	JCurl.get("some.url", JCurlCookieManager.getInstance());
	JCurl.get("some.url/here?we=need&cookies", JCurlCookieManager.getInstance());



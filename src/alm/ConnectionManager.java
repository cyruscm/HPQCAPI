package alm;

import infrastructure.RestConnector;
import infrastructure.Base64Encoder;
import infrastructure.Entities;
import infrastructure.Entity;
import infrastructure.EntityMarshallingUtils;
import infrastructure.Response;
import test.Assert;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;


public class ConnectionManager {

    private RestConnector con;
    private String URL;

    public ConnectionManager() {
        URL = "http://" + Config.getHost() + ":" + Config.getPort() + "/qcbin";
        con = RestConnector.getInstance();
        con = con.init(new HashMap<String, String>(),
                URL,
                Config.getDomain(),
                Config.getProject());
    }

    /**
     * Processed login procedure that validates cookies and removes pre-existing sessions
     */
    public void validatedLogin() {
        if (isAuthenticated()) {
            logout();
            con.setCookies(new HashMap<String, String>());
        }
        Assert.assertTrue("Login did not return HTTP_OK", login());
        Assert.assertTrue(
                "login did not cause creation of Light Weight Single Sign On(LWSSO) cookie.",
                con.getCookieString().contains("LWSSO_COOKIE_KEY"));
        Assert.assertTrue("QCSession cookies were not established", establishQCSession());
        Assert.assertTrue(
                "QCSession establishment did not cause creation of QCSession cookie.",
                con.getCookieString().contains("QCSession"));
        Assert.assertTrue(
                "QCSession establishment  did not cause creation of XSRF-TOKEN cookie.",
                con.getCookieString().contains("XSRF-TOKEN"));

        Assert.assertTrue("Credentials did not authenticate.", isAuthenticated());
    }

    /**
     * Verify if connection manager is currently authenticated with ALM
     *
     * @return True if authenticated, false if not
     */
    public boolean isAuthenticated() {
        Response response;
        try {
            response = con.httpGet(con.buildUrl(Endpoints.ISAUTHENTICATED), null, null);
        } catch (Exception e) {
            logError("Authentication resulted in error " + e.toString(), e);
            return false;
        }
        int responseCode = response.getStatusCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return true;
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return false;
        } else {
            logError("Authentication resulted in error " +  response.getFailure().toString(), response.getFailure());
            return false;
        }
    }


    /**
     * Logs into ALM
     *
     * @return True if login procedure returned HTTP_OK
     * @throws Exception
     */
    public boolean login() {
        byte[] credBytes = (Config.getUsername() + ":" + Config.getPassword()).getBytes();
        String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);

        Map<String, String> map = new HashMap<String, String>();
        map.put("Authorization", credEncodedString);
        try {
            Response response = con.httpGet(con.buildUrl(Endpoints.AUTHENTICATE), null, map);
            return (response.getStatusCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            logError("QCSession establishment resulted in error " + e.toString(), e);
            return false;
        }
    }

    /**
     * Builds a QCSession cookie and an XSRF-TOKEN cookie.
     * Requires a LWSSO Cookue to be in place
     *
     * @return True if 201 code is returned, false if not.
     */
    public boolean establishQCSession() {
        String QCSessionUrl = con.buildUrl(Endpoints.SITESESSION);
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/xml");
        requestHeaders.put("Content-Length", "0");
        try {
            Response resp = con.httpPost(QCSessionUrl, null, requestHeaders);
            return (resp.getStatusCode() == HttpURLConnection.HTTP_CREATED);
        } catch (Exception e) {
            logError("QCSession establishment resulted in error " + e.toString(), e);
            return false;
        }
    }


    /**
     * @return true if logout successful
     */
    public boolean logout() {

        //note the get operation logs us out by setting authentication cookies to:
        // LWSSO_COOKIE_KEY="" via server response header Set-Cookie
        try {
            Response response = con.httpGet(con.buildUrl(Endpoints.LOGOUT),
                    null, null);
            return (response.getStatusCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            logError("Logout resulted in error " + e.toString(), e);
            return false;
        }


    }


    private void logError(String errorMessage, Exception e) {
        Logger.logError(errorMessage);
        Logger.logError(e.getMessage());
    }


    /**
     * Gets the provided collectionUrl and returns the body response.
     * return is null on authentication error
     * @param collectionUrl endpoint to connect to.
     * @return Body response
     * @throws Exception
     */
    public Entity readEntity(String endpoint) throws Exception{
        String collectionUrl = con.buildUrl(endpoint);
    	
    	if (!isAuthenticated()) {
            Logger.logWarning("Not authenticated, exiting readEntity(" + collectionUrl + ")");
            return null;
        }
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/xml");

        Response response = con.httpGet(con.buildUrl(collectionUrl), null, requestHeaders);
        Assert.assertEquals("Resource read did not return HTTP_OK", response.getStatusCode(), HttpURLConnection.HTTP_OK);

		return EntityMarshallingUtils.marshal(Entity.class, response.toString());
    }


    /**
     * Sends a post to the collectionUrl and returns the location url
     * @param collectionUrl Endpoint to post to
     * @param postedEntityXml xml data to post
     * @return posted location url
     * @throws Exception
     */
    public String createEntity(String endpoint, String postedEntityXml)
            throws Exception {
    	String collectionUrl = con.buildUrl(endpoint);

        if (!isAuthenticated()) {
            Logger.logWarning("Not authenticated, exiting createEntity(" + collectionUrl + ")");
            return null;
        }
        
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/xml");
        requestHeaders.put("Accept", "application/xml");

        // As can be seen in the implementation below, creating an entity
        //is simply posting its xml into the correct collection.
        Response response = con.httpPost(collectionUrl,
                postedEntityXml.getBytes(), requestHeaders);

        Exception failure = response.getFailure();
        if (failure != null) {
            throw failure;
        }

        /*
         Note that we also get the xml of the newly created entity.
         at the same time we get the url where it was created in a
         location response header.
        */
        String entityUrl =
                response.getResponseHeaders().get("Location").iterator().next();

        return entityUrl;
    }

    
    /**
     * Sends a put request to the provided collectionUrl
     * @param collectionUrl endpoint to update
     * @param postedEntityXml xml to provide in put
     * @return posted location url.
     * @throws Exception
     */
    public String updateEntity(String endpoint, String postedEntityXml)
            throws Exception {
    	String collectionUrl = con.buildUrl(endpoint);
    	
        if (!isAuthenticated()) {
            Logger.logWarning("Not authenticated, exiting updateEntity(" + collectionUrl + ")");
            return null;
        }

        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/xml");
        requestHeaders.put("Accept", "application/xml");

        // As can be seen in the implementation below, creating an entity
        //is simply posting its xml into the correct collection.
        Response response = con.httpPut(collectionUrl,
                postedEntityXml.getBytes(), requestHeaders);

        Exception failure = response.getFailure();
        if (failure != null) {
            throw failure;
        }

        /*
         Note that we also get the xml of the newly created entity.
         at the same time we get the url where it was created in a
         location response header.
        */
        String entityUrl =
                response.getResponseHeaders().get("Location").iterator().next();

        return entityUrl;
    }


	public Entities queryEntity(String endpoint, Map<String, String> queryParams) throws Exception { 
    	String collectionUrl = con.buildUrl(endpoint);
    	
        if (!isAuthenticated()) {
            Logger.logWarning("Not authenticated, exiting queryEndpoint(" + collectionUrl + ")");
            return null;
        }
		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Accept", "application/xml");
		
		StringBuilder b = new StringBuilder();
		if (queryParams != null && queryParams.size() > 0) {
			b.append("query=");
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				b.append("{" + entry.getKey() + "[" + entry.getValue() + "]}&");
			}
		} else {
			Logger.logWarning("queryEndpoint should not be used with empty query parameters. Use readEntity");
		}

		String response = con.httpGet(con.buildUrl(endpoint), b.toString(), requestHeaders).toString();
		Logger.logDebug(response);
		return EntityMarshallingUtils.marshal(Entities.class, response);
	}
}

package com.tyson.hpqcjapi.utils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.hpe.infrastructure.Base64Encoder;
import com.hpe.infrastructure.Entity;
import com.hpe.infrastructure.EntityMarshallingUtils;
import com.hpe.infrastructure.Response;
import com.hpe.infrastructure.RestConnector;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.resources.Endpoints;
import com.tyson.hpqcjapi.resources.Messages;
import com.tyson.hpqcjapi.types.Entities;

/**
 * Servers as a wrapper to {@link RestConnector} creating context methods in the
 * application of HP ALM. All functions should be based on ALM Rest
 * datastructures and should not be specialized to specific features (e.g. tests
 * vs test-sets as both are entity collections) except in areas lacking generic
 * responses (authentication).
 * 
 * @author MARTINCORB
 *
 */
public class ConnectionManager {

	protected RestConnector con;
	protected Response lastResponse;

	/**
	 * Constructs a ConnectionManager that is prepared around the provided HP ALM
	 * domain
	 */
	public ConnectionManager() {
		String URL = "http://" + Config.getHost() + ":" + Config.getPort() + "/qcbin";
		con = RestConnector.getInstance();
		con = con.init(new HashMap<String, String>(), URL, Config.getDomain(), Config.getProject());
		lastResponse = null;
	}

	/**
	 * Processed login procedure that validates cookies and removes pre-existing
	 * sessions. If this fails, then something is either wrong with this program or
	 * the ALM version.
	 * 
	 * @return True if login succeeded, false if not.
	 */
	public boolean validatedLogin() {
		if (isAuthenticated()) {
			Logger.logDebug("Connection already validated, clearing cookies and re-establishing auth");
			logout();
			con.setCookies(new HashMap<String, String>());
		}
		if (!login()) {
			Logger.logError("Authentication did not return valid response. Check credentials.");
			return false;
		}

		if (!establishQCSession()) {
			Logger.logError("Site-session did not return a valid response. Verify provided ALM version is 12.01.");
			return false;
		}

		for (String cookieName : Messages.EXPECTED_SITE_SESSION_COOKIES) {
			if (con.getCookieString().contains(cookieName)) {
				Logger.logError("Site-session post returned 200 but did not establish neccesary cookies (" + cookieName
						+ "). Verify provided ALM version is 12.01");
				return false;
			}
		}

		if (!isAuthenticated()) {
			Logger.logError(
					"All checks passed but is-authenticated endpoint returning incorrect information. Verify provided ALM version is 12.01");
			return false;
		}

		Logger.logDebug("All validated login checks passed");
		return true;
	}

	/**
	 * Verify if connection manager is currently authenticated with ALM
	 * 
	 * @return True if authenticated, false if not
	 */
	public boolean isAuthenticated() {
		Response response;

		try {
			response = con.httpGet(con.buildUrl(Endpoints.IS_AUTHENTICATED), null, null);
			lastResponse = response;
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("isAuthenticated", Endpoints.IS_AUTHENTICATED), e);
			return false;
		}

		if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
			return true;
		} else if (response.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			return false;
		} else {
			logError("Authentication resulted in error " + response.getFailure().toString(), response.getFailure());
			return false;
		}
	}

	/**
	 * Logs into ALM
	 *
	 * @return True if login procedure returned HTTP_OK
	 */
	public boolean login() {
		byte[] credBytes = (Config.getUsername() + ":" + Config.getPassword()).getBytes();
		String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);

		Map<String, String> map = new HashMap<String, String>();
		map.put("Authorization", credEncodedString);

		boolean success = false;

		try {
			Response response = con.httpGet(con.buildUrl(Endpoints.AUTHENTICATE), null, map);
			lastResponse = response;

			success = (response.getStatusCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("login", Endpoints.AUTHENTICATE), e);
		}

		return success;
	}

	/**
	 * Builds a QCSession cookie and an XSRF-TOKEN cookie. Requires a LWSSO Cookue
	 * to be in place
	 *
	 * @return True if 201 code is returned, false if not.
	 */
	public boolean establishQCSession() {
		String QCSessionUrl = con.buildUrl(Endpoints.SITE_SESSION);

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Accept", "application/xml");

		// 411 Requirement by 12.01 SITE_SESSION endpoint
		requestHeaders.put("Content-Length", "0");

		boolean success = false;

		try {
			Response response = con.httpPost(QCSessionUrl, null, requestHeaders);
			lastResponse = response;
			success = (response.getStatusCode() == HttpURLConnection.HTTP_CREATED);
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("establishQCSession", Endpoints.SITE_SESSION), e);
		}

		return success;
	}

	/**
	 * @return true if logout successful
	 */
	public boolean logout() {

		boolean success = false;

		try {
			Response response = con.httpGet(con.buildUrl(Endpoints.LOGOUT), null, null);
			lastResponse = response;
			con.setCookies(new HashMap<String, String>());
			success = (response.getStatusCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("logout", Endpoints.LOGOUT), e);
		}

		return success;
	}

	/**
	 * @param errorMessage
	 * @param e
	 */
	protected void logError(String errorMessage, Exception e) {
		Logger.logError(errorMessage);
		Logger.logError(e.getMessage());
	}

	/**
	 * Sends a post to the endpoint. Endpoint compliant standards are not provided
	 * in this context.
	 * 
	 * @param endpoint
	 *            Endpoint to post to
	 * @param postedEntityXml
	 *            xml data to post
	 * @return Newly created entity data
	 */
	public Entity createEntity(String endpoint, String postedEntityXml) {
		String collectionUrl = con.buildUrl(endpoint);

		if (!isAuthenticated()) {
			Logger.logWarning("Not authenticated, exiting createEntity(" + collectionUrl + ")");
			return null;
		}

		byte[] XmlBytes = null;
		Map<String, String> requestHeaders = new HashMap<String, String>();
		if (postedEntityXml != null && postedEntityXml.length() > 0) {
			requestHeaders.put("Content-Type", "application/xml");
			XmlBytes = postedEntityXml.getBytes();
		}
		requestHeaders.put("Accept", "application/xml");

		Entity entity = null;
		try {
			Response response = con.httpPost(collectionUrl, XmlBytes, requestHeaders);
			lastResponse = response;
			if (response.getStatusCode() == HttpURLConnection.HTTP_CREATED) {
				entity = EntityMarshallingUtils.marshal(Entity.class, response.toString());
			} else {
				Logger.logError(Messages.INCORRECT_RESPONSE_CODE("Read Entity", "" + response.getStatusCode(),
						"" + HttpURLConnection.HTTP_OK));
			}
		} catch (Exception e) {
			logError("Create Entity at " + endpoint + "resulted in error.", e);
		}

		return entity;
	}

	/**
	 * Gets the provided endpoint and returns the body response. return is null on
	 * error
	 * 
	 * @param endpoint
	 *            endpoint to connect to.
	 * @return Body response, or null
	 */
	public Entity readEntity(String endpoint) {
		String collectionUrl = con.buildUrl(endpoint);

		if (!isAuthenticated()) {
			Logger.logWarning("Not authenticated, exiting readEntity(" + collectionUrl + ")");
			return null;
		}
		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Accept", "application/xml");

		Entity entity = null;

		try {
			Response response = con.httpGet(collectionUrl, null, requestHeaders);
			lastResponse = response;

			if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
				entity = EntityMarshallingUtils.marshal(Entity.class, response.toString());
			} else {
				Logger.logError(Messages.INCORRECT_RESPONSE_CODE("Read Entity", "" + response.getStatusCode(),
						"" + HttpURLConnection.HTTP_OK));
			}
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("readEntity", endpoint), e);
		}

		return entity;
	}

	/**
	 * Sends a put request to the provided collectionUrl
	 * 
	 * @param collectionUrl
	 *            endpoint to update
	 * @param postedEntityXml
	 *            xml to provide in put
	 * @return posted location url.
	 */
	public Entity updateEntity(String endpoint, String postedEntityXml) {

		if (!isAuthenticated()) {
			Logger.logWarning("Not authenticated, exiting updateEntity(" + endpoint + ")");
			return null;
		}

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Content-Type", "application/xml");
		requestHeaders.put("Accept", "application/xml");

		Entity entity = null;
		try {
			Response response = con.httpPut(con.buildUrl(endpoint), postedEntityXml.getBytes(), requestHeaders);
			lastResponse = response;
			if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
				entity = EntityMarshallingUtils.marshal(Entity.class, response.toString());
			} else {
				Logger.logError(Messages.INCORRECT_RESPONSE_CODE("Read Entity", "" + response.getStatusCode(),
						"" + HttpURLConnection.HTTP_OK));
			}
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("updateEntity", endpoint), e);
		}
		return entity;
	}

	/**
	 * Sends a delete request to endpoint
	 * 
	 * @param endpoint
	 *            entity to delete
	 * @return The deleted entity, or null
	 */
	public Entity deleteEntity(String endpoint) {
		if (!isAuthenticated()) {
			Logger.logWarning("Not authenticated, exiting updateEntity(" + endpoint + ")");
			return null;
		}

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Content-Type", "application/xml");
		requestHeaders.put("Accept", "application/xml");

		Entity entity = null;
		try {
			Response response = con.httpDelete(con.buildUrl(endpoint), requestHeaders);
			lastResponse = response;
			if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
				entity = EntityMarshallingUtils.marshal(Entity.class, response.toString());
			} else {
				Logger.logError(Messages.INCORRECT_RESPONSE_CODE("Delete Entity", "" + response.getStatusCode(),
						"" + HttpURLConnection.HTTP_OK));
			}
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("deleteEntity", endpoint), e);
		}
		return entity;

	}

	/**
	 * Sends a get request to a collection endpoint with query results
	 * 
	 * @param endpoint
	 *            Endpoint to communicate with
	 * @param queryParams
	 *            Map of name and values to query for
	 * @return The matching entities, or null
	 */
	public Entities readCollection(String endpoint, Map<String, String> queryParams) {
		String query = null;
		if (queryParams != null && queryParams.size() > 0) {
			StringBuilder b = new StringBuilder();
			b.append("query=");
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				b.append("{" + entry.getKey() + "[" + entry.getValue() + "]}&");
			}
			query = b.toString();
		}

		return readCollection(endpoint, query);
	}

	public Entities readCollection(String endpoint, String queryString) {
		if (!isAuthenticated()) {
			Logger.logWarning("Not authenticated, exiting readCollection(" + endpoint + ")");
			return null;
		}

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Accept", "application/xml");

		Entities entities = null;
		try {
			Response response = con.httpGet(con.buildUrl(endpoint), queryString, requestHeaders);
			lastResponse = response;
			if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
				entities = EntityMarshallingUtils.marshal(Entities.class, response.toString());
			} else {
				Logger.logError(Messages.INCORRECT_RESPONSE_CODE("Read Collection", "" + response.getStatusCode(),
						"" + HttpURLConnection.HTTP_OK));
			}
		} catch (Exception e) {
			logError(Messages.UNEXPECTED_ERROR("queryCollection", endpoint), e);
		}

		return entities;
	}
}

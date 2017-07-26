package com.tyson.hpqcjapi.utils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.resources.Endpoints;
import com.tyson.hpqcjapi.resources.Messages;
import com.tyson.hpqcjapi.types.Entities;

import infrastructure.Entity;
import infrastructure.Entity.Fields;
import infrastructure.Entity.Fields.Field;

public class JUnitPoster {

	ConnectionManager con;
	
	public enum Response {
		OK, RETRY, FAILURE, MISSING, DUPLICATE
	}
	
	private Response responseStatus;
	
	public JUnitPoster() {
		con = new ConnectionManager();
		responseStatus = Response.OK;
	}
	
	public void init() {
		try {
			con.validatedLogin();
		} catch (RuntimeException e) {
			e.printStackTrace();
			responseStatus = Response.FAILURE;
		}
		responseStatus = Response.OK;
	}
	
	
	public Response getResponse() {
		return responseStatus;
	}
	
	public String createTest(String name) {
		String toReturn = null;

		XMLCreator xml = new XMLCreator("Entity", "test");
		xml.addField("name", name);
		xml.addField("user-02", Config.getTeam());
		xml.addField("description", Messages.DEFAULT_DESCRIPTION(name));
		xml.addField("parent-id", Config.getUnitTestFolderID());
		xml.addField("subtype-id", "manual");
		infrastructure.Response response = con.createEntity(Endpoints.TESTS, xml.publish());
		
		if (response.getStatusCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
			if (new String(response.getResponseData()).contains(Messages.TEST_ALREADY_EXISTS)) {
				responseStatus = Response.DUPLICATE;
			} else {
				responseStatus = Response.FAILURE;
			}
		} else if (response.getStatusCode() == HttpURLConnection.HTTP_CREATED) {
			responseStatus = Response.OK;
			toReturn = URLToEndpoint(response.getResponseHeaders().get("Location").iterator().next());
		} else {
			responseStatus = Response.FAILURE;
		}

		return toReturn;
	}
	
	public String URLToEndpoint(String URL) {
		return URL.replaceFirst("(http|https)://" + Config.getHost() + "/qcbin/" , "");
	}
	
	
	public boolean checkInTest(String testID) {
		return versionControlTest(testID, Endpoints.CHECKIN_TEST(testID), null);
	}
	
	public boolean checkOutTest(String testID) {
		return versionControlTest(testID, Endpoints.CHECKOUT_TEST(testID), Messages.CHECKOUT_MESSAGE);
	}
	
	private boolean versionControlTest(String testID, String endpoint, String comment) {
		String data = null;

		if (comment != null) {
			XMLCreator xml = new XMLCreator("CheckOutParameters", null);
			xml.addCustomValue("Comment", comment);
			data = xml.publish();
		}
		
		infrastructure.Response response = con.createEntity(endpoint, data);

		if (response.getStatusCode() == HttpURLConnection.HTTP_CREATED) {
			responseStatus = Response.OK;
			return true;
		} else {
			responseStatus = Response.FAILURE;
			return false;
		}
	}
	
	
	public String[] getCurrentRunSteps(String testID) {
		return null;
	}

	
	public String getTestID(String name) {
		String toReturn = null;
		try {
			Map<String, String> queryParams = new HashMap<String, String>();
			queryParams.put("name", name);
			Entities entities = con.queryCollection(Endpoints.TESTS, queryParams);
			
			if (entities.Count() == 0) {
				Logger.logWarning("Test with name " + name + " doesn't exist. Please create one.");
				responseStatus = Response.MISSING;
				toReturn = null;
			} else if (entities.Count() > 1) {
				Logger.logWarning("Test with name " + name + " has more than one matching test.");
				responseStatus = Response.DUPLICATE;
				toReturn = null;
			} else {
				Logger.logWarning("1 found...");
				List<Field> fields = entities.getEntities().get(0).getFields().getField();
				for (Field field : fields) {
					if (field.getName().equals("id")) {
						toReturn = field.getValue().get(0);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.logError(e.toString());
			toReturn = null;
		}
		return toReturn;
	}
	
}

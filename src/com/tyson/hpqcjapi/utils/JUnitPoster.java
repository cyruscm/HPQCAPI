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

public class JUnitPoster extends ConnectionManager {

	public enum Response {
		OK, RETRY, FAILURE, MISSING, DUPLICATE, VCERROR
	}
	
	private Response responseStatus;
	
	public JUnitPoster() {
		super();
		responseStatus = Response.OK;
	}
	
	public void init() {
		try {
			this.validatedLogin();
		} catch (RuntimeException e) {
			e.printStackTrace();
			responseStatus = Response.FAILURE;
		}
		responseStatus = Response.OK;
	}
	
	
	public Response getResponse() {
		return responseStatus;
	}
	
	public Entity createTest(String name) {
		XMLCreator xml = new XMLCreator("Entity", "test");
		xml.addField("name", name);
		xml.addField("user-02", Config.getTeam());
		xml.addField("description", Messages.DEFAULT_DESCRIPTION(name));
		xml.addField("parent-id", Config.getUnitTestFolderID());
		xml.addField("subtype-id", "manual");
		Entity entity = this.createEntity(Endpoints.TESTS, xml.publish());
		
		if (entity == null) {
			if (new String(lastResponse.getResponseData()).contains(Messages.TEST_ALREADY_EXISTS)) {
				responseStatus = Response.DUPLICATE;
			} else {
				responseStatus = Response.FAILURE;
			}
		}

		return entity;
	}
	
	
	public String getTestID(String name) {
		String toReturn = null;
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("name", name);
		Entities entities = this.queryCollection(Endpoints.TESTS, queryParams);
		
		if (entities == null) {
			responseStatus = Response.FAILURE;
			return null;
		}
		
		if (entities.Count() == 0) {
			Logger.logWarning("Test with name " + name + " doesn't exist. Please create one.");
			responseStatus = Response.MISSING;
			toReturn = null;
		} else if (entities.Count() > 1) {
			Logger.logWarning("Test with name " + name + " has more than one matching test. Somethings probably broken.");
			responseStatus = Response.DUPLICATE;
			toReturn = null;
		} else {
			List<Field> fields = entities.getEntities().get(0).getFields().getField();
			responseStatus = Response.FAILURE;
			for (Field field : fields) {
				if (field.getName().equals("id")) {
					toReturn = field.getValue().get(0);
					responseStatus = Response.OK;
					break;
				}
			}
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
		
		Entity entity = this.createEntity(endpoint, data);

		if (entity != null) {
			responseStatus = Response.OK;
			return true;
		} else {
			responseStatus = Response.FAILURE;
			return false;
		}
	}
	
	
	public Entity updateRunStep(String id, String postedEntityXml) {
		Entity entity = updateEntity(Endpoints.DESIGN_STEP(id), postedEntityXml);
		
		if (entity == null) {
			if (new String(lastResponse.getResponseData()).contains(Messages.TEST_ALREADY_EXISTS)) {
				Logger.logWarning("Failed to update step. Make sure you check out the entity first.");
				responseStatus = Response.VCERROR;
			} else {
				responseStatus = Response.FAILURE;
			}
		} else {
			responseStatus = Response.OK;
		}
		
		return entity;
	}
	
	
	public Entity getRunStep(String id) {
		Entity entity = readEntity(Endpoints.DESIGN_STEP(id));
		if (entity == null) {
			if (new String(lastResponse.getResponseData()).contains(Messages.ENTITY_MISSING)) {
				responseStatus = Response.MISSING;
			} else {
				responseStatus = Response.FAILURE;
			}
		} else {
			responseStatus = Response.OK;
		}
		
		return entity;
	}
	
	
	public Entity addRunStep(String name, String parentId, String description, String expected) {
		XMLCreator xml = new XMLCreator("Entity", "design-step");
		xml.addField("name", name);
		xml.addField("parent-id", parentId);
		xml.addField("description", description);
		xml.addField("expected", expected);
		
		Entity entity = createEntity(Endpoints.DESIGN_STEPS, xml.publish());
		if (entity == null) {
			if (new String(lastResponse.getResponseData()).contains(Messages.VC_NOT_CHECKED_OUT)) {
				responseStatus = Response.VCERROR;
			} else {
				responseStatus = Response.FAILURE;
			}
		} else {
			responseStatus = Response.OK;
		}
		
		return entity;
	}
	
	
	public Entities getCurrentRunSteps(String testID) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("test-id", testID);
		Entities entities = queryCollection(Endpoints.DESIGN_STEPS, queryParams);
		
		if (entities == null) {
			responseStatus = Response.FAILURE;
		} else {
			responseStatus = Response.OK;
		}
		
		return entities;
	}

	public Entities queryTestSets(Map<String, String> queryParameters) {
		Entities entities = queryCollection(Endpoints.TEST_SETS, queryParameters);
		if (entities == null) {
			responseStatus = Response.FAILURE;
		} else {
			responseStatus = Response.OK;
		}
		
		return entities;
	}
	
	public Entities queryTestSetFolders(Map<String, String> queryParameters) {
		Entities entities = queryCollection(Endpoints.TEST_SET_FOLDERS, queryParameters);
		if (entities == null) {
			responseStatus = Response.FAILURE;
		} else {
			responseStatus = Response.OK;
		}
		
		return entities;	
	}
	
	public Entity getTestSet(String id) {
		Entity entity = readEntity(Endpoints.TEST_SET(id));
		if (entity == null) {
			if (new String(lastResponse.getResponseData()).contains(Messages.ENTITY_MISSING)) {
				responseStatus = Response.MISSING;
			} else {
				responseStatus = Response.FAILURE;
			}
		} else {
			responseStatus = Response.OK;
		}
		
		return entity;
	}
	
}

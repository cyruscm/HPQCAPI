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
import infrastructure.Entity.Fields.Field;

/**
 * This specializes the ConnectionManager towards ALM 12.01 functions with CRUD. However,
 * it does not specialize to use cases. Either create another layer or use
 * wrapper to have specific case applications. However, each utility context is
 * expected inside the method. Such as if a specific CRUD request requires
 * certain fields in body, they will be passed as method parameters instead of
 * generic maps. EXCEPT in update requests to allow a greater level of
 * modification. (This is also done to save the annoyance of null check for
 * updating).
 * 
 * INVARIANTS: 
 * 1. XML Mapped objects (entities/entity/etc) should
 * be the only external receivable types.
 * 
 * 2. Every method with objects for output should use null to symbolize error.
 * 
 * @author MARTINCORB
 *
 */
public class ALMManager extends ConnectionManager {

	public enum Response {
		OK, RETRY, FAILURE, MISSING, DUPLICATE, VCERROR, NOAUTH, NOPERM, BADINPUT
	}

	private Response responseStatus;
	private Map<Response, String> errorResponseMap;

	public ALMManager() {
		super();
		responseStatus = Response.OK;
		initErrorResponseMap();
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
	
	private void initErrorResponseMap() {
		errorResponseMap = new HashMap<Response, String>();
		errorResponseMap.put(Response.VCERROR, "qccore.check-in-failure");
		errorResponseMap.put(Response.VCERROR, "qccore.check-out-failure");
		errorResponseMap.put(Response.MISSING, "qccore.entity-not-found");
		errorResponseMap.put(Response.VCERROR, "qccore.lock-failure");
		errorResponseMap.put(Response.NOPERM, "qccore.operation-forbidden");
		errorResponseMap.put(Response.NOAUTH, "qccore.session-has-expired");
		errorResponseMap.put(Response.BADINPUT, "qccore.unknown-field-name");
		errorResponseMap.put(Response.BADINPUT, "qccore.required-field-missing");
	}

	public Response getResponse() {
		return responseStatus;
	}

	public String URLToEndpoint(String URL) {
		return URL.replaceFirst("(http|https)://" + Config.getHost() + "/qcbin/", "");
	}

	// ================================================================================
	// Generics
	// ================================================================================
	
	private String mapToXMLString(Map<String, String> map) {
		return mapToXMLString(map, "Entity", null);
	}

	private String mapToXMLString(Map<String, String> map, String subType) {
		return mapToXMLString(map, "Entity, subType");
	}
	
	private String mapToXMLString(Map<String, String> map, String type, String subType) {
		XMLCreator xml = new XMLCreator(type, subType);
		return appendMapToXML(xml, map).publish();
	}
	
	private XMLCreator appendMapToXML(XMLCreator xml, Map<String, String> map) {
		if (map != null) {
			for (Map.Entry<String, String> param : map.entrySet()) {
				xml.addField(param.getKey(), param.getValue());
			}
		}	
		return xml;
	}
	
	private boolean lastResponseContains(String message) {
		if (lastResponse == null) {
			return false;
		}

		return (new String(lastResponse.getResponseData()).contains(message));
	}

	private Response genericResponseHandler(Object obj, Map<Response, String> responseMap) {
		if (obj == null) {
			if (lastResponse.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				return Response.NOAUTH;
			} 
			
			if (responseMap != null) {
				for (Map.Entry<Response, String> entry : responseMap.entrySet()) {
					if (lastResponseContains(entry.getValue())) {
						return entry.getKey();
					}
				}
			}
			
			for (Map.Entry<Response, String> entry : errorResponseMap.entrySet()) {
				if (lastResponseContains(entry.getValue())) {
					return entry.getKey();
				}
			}
			
			return Response.FAILURE;
		} else {
			return Response.OK;
		}
	}

	private Entity genericCreateEntity(String endpoint, String xml, Map<Response, String> responseMap) {
		Entity entity = createEntity(endpoint, xml);
		responseStatus = genericResponseHandler(entity, responseMap);
		return entity;
	}

	private Entity genericGetEntity(String endpoint, Map<Response, String> responseMap) {
		Entity entity = readEntity(endpoint);
		responseStatus = genericResponseHandler(entity, responseMap);
		return entity;
	}

	private Entity genericUpdateEntity(String endpoint, String xml, Map<Response, String> responseMap) {
		Entity entity = updateEntity(endpoint, xml);
		responseStatus = genericResponseHandler(entity, responseMap);
		return entity;
	}
	
	private Entity genericDeleteEntity(String endpoint, Map<Response, String> responseMap) {
		Entity entity = deleteEntity(endpoint);
		responseStatus = genericResponseHandler(entity, responseMap);
		return entity;
	}

	private Entities genericReadCollection(String endpoint, Map<String, String> query,
			Map<Response, String> responseMap) {
		Entities entities = readCollection(endpoint, query);
		responseStatus = genericResponseHandler(entities, responseMap);
		return entities;
	}

	// ================================================================================
	// Tests
	// ================================================================================

	public Entity createTest(String name, Map<String, String> extraParams) {
		XMLCreator xml = new XMLCreator("Entity", "test");
		xml.addField("name", name);
		xml.addField("user-02", Config.getTeam());
		xml.addField("description", Messages.DEFAULT_DESCRIPTION(name));
		xml.addField("parent-id", Config.getUnitTestFolderID());
		xml.addField("subtype-id", "manual");

		this.appendMapToXML(xml, extraParams);
		
		return genericCreateEntity(Endpoints.TESTS, xml.publish(), null);
	}
	
	public Entity getTest(String id) {
		return genericGetEntity(Endpoints.TEST(id), null);
	}
	
	public Entity updateTest(String id, String postedEntityXML) {
		return genericUpdateEntity(Endpoints.TEST(id), postedEntityXML, null);
	}
	
	public Entity deleteTest(String id) {
		return genericDeleteEntity(Endpoints.TEST(id), null);
	}
	
	public Entities getTestCollection(Map<String, String> queryParameters) {
		return genericReadCollection(Endpoints.TESTS, queryParameters, null);
	}

	public String getTestID(String name) {
		String toReturn = null;
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("name", name);

		Entities entities = genericReadCollection(Endpoints.TESTS, queryParams, null);

		if (entities.Count() == 0) {
			Logger.logWarning("Test with name " + name + " doesn't exist. Please create one.");
			responseStatus = Response.MISSING;
			toReturn = null;
		} else if (entities.Count() > 1) {
			Logger.logWarning(
					"Test with name " + name + " has more than one matching test. Somethings probably broken.");
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
	// ================================================================================
	// Version Control and Tools
	// ================================================================================

	public boolean checkInTest(String testID) {
		return genericVersionControl(testID, Endpoints.CHECKIN_TEST(testID), null);
	}

	public boolean checkOutTest(String testID) {
		return genericVersionControl(testID, Endpoints.CHECKOUT_TEST(testID), Messages.CHECKOUT_MESSAGE);
	}

	private boolean genericVersionControl(String testID, String endpoint, String comment) {
		String data = null;

		if (comment != null) {
			XMLCreator xml = new XMLCreator("CheckOutParameters", null);
			xml.addCustomValue("Comment", comment);
			data = xml.publish();
		}

		genericCreateEntity(endpoint, data, null);
		return (getResponse() == Response.OK);
	}

	// ================================================================================
	// Design Steps
	// ================================================================================

	public Entity createDesignStep(String name, String parentId, String description, String expected, Map<String, String> extraParams) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.VCERROR, Messages.VC_NOT_CHECKED_OUT);

		XMLCreator xml = new XMLCreator("Entity", "design-step");
		xml.addField("name", name);
		xml.addField("parent-id", parentId);
		xml.addField("description", description);
		xml.addField("expected", expected);
		
		this.appendMapToXML(xml, extraParams);

		return genericCreateEntity(Endpoints.DESIGN_STEPS, xml.publish(), responseMap);
	}

	public Entity getDesignStep(String id) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.MISSING, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.DESIGN_STEP(id), responseMap);
	}
	
	public Entity updateDesignStep(String id, String postedEntityXml) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.VCERROR, Messages.DESIGN_NOT_CHECKED_OUT);
		return genericUpdateEntity(Endpoints.DESIGN_STEP(id), postedEntityXml, responseMap);
	}
	
	public Entity deleteDesignStep(String id) {
		return genericDeleteEntity(Endpoints.DESIGN_STEP(id), null);
	}
	
	public Entities getDesignSteps(Map<String, String> queryParameters) {
		return genericReadCollection(Endpoints.DESIGN_STEPS, queryParameters, null);
	}

	//TODO REPLACE ME
	public Entities getCurrentDesignSteps(String testID) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("test-id", testID);
		return genericReadCollection(Endpoints.DESIGN_STEPS, queryParams, null);
	}

	// ================================================================================
	// Test Sets
	// ================================================================================

	//TODO Find all needed vars
	public Entity createTestSet(String name, Map<String, String> extraParams) {
		XMLCreator xml = new XMLCreator("Entity", "test-set");
		xml.addField("name", name);
		
		this.appendMapToXML(xml, extraParams);
		
		return genericCreateEntity(Endpoints.TEST_SETS, xml.publish(), null);
	}
	
	public Entity getTestSet(String id) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.MISSING, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.TEST_SET(id), responseMap);
	}
	
	
	public Entities queryTestSets(Map<String, String> queryParameters) {
		return genericReadCollection(Endpoints.TEST_SETS, queryParameters, null);
	}

	public Entities queryTestSetFolders(Map<String, String> queryParameters) {
		return genericReadCollection(Endpoints.TEST_SET_FOLDERS, queryParameters, null);
	}

	
	// ================================================================================
	// Runs
	// ================================================================================

	public Entity createRun(String name, String parentId, String description, String expected) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.VCERROR, Messages.VC_NOT_CHECKED_OUT);

		XMLCreator xml = new XMLCreator("Entity", "run");
		xml.addField("name", name);
		xml.addField("parent-id", parentId);
		xml.addField("description", description);
		xml.addField("expected", expected);

		return genericCreateEntity(Endpoints.RUNS, xml.publish(), responseMap);
	}

	public Entity getRun(String id) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.MISSING, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.RUN(id), responseMap);
	}

	public Entity updateRun(String id, String postedEntityXml) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.VCERROR, Messages.DESIGN_NOT_CHECKED_OUT);
		return genericUpdateEntity(Endpoints.RUN(id), postedEntityXml, responseMap);
	}
	
	public Entity deleteRun(String id) {
		return genericDeleteEntity(Endpoints.RUN(id), null);
	}

	public Entities queryRuns(Map<String, String> queryParameters) {
		return genericReadCollection(Endpoints.RUNS, queryParameters, null);
	}

	// ================================================================================
	// Run Steps
	// ================================================================================

	public Entity createRunStep(String name, String parentId, String description, String expected) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.VCERROR, Messages.VC_NOT_CHECKED_OUT);

		XMLCreator xml = new XMLCreator("Entity", "run-step");
		xml.addField("name", name);
		xml.addField("parent-id", parentId);
		xml.addField("description", description);
		xml.addField("expected", expected);
		return genericCreateEntity(Endpoints.DESIGN_STEPS, xml.publish(), responseMap);
	}

	public Entity getRunStep(String parentId, String id) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.MISSING, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.RUN_STEP(parentId, id), responseMap);
	}

	public Entity updateRunStep(String parentId, String id, String postedEntityXml) {
		Map<Response, String> responseMap = new HashMap<Response, String>();
		responseMap.put(Response.VCERROR, Messages.DESIGN_NOT_CHECKED_OUT);
		return genericUpdateEntity(Endpoints.RUN_STEP(parentId, id), postedEntityXml, responseMap);
	}
	
	public Entity deleteRunStep(String parentId, String id) {
		return genericDeleteEntity(Endpoints.RUN_STEP(parentId, id), null);
	}
	
	public Entities getCurrentRunSteps(String testID) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("test-id", testID);
		return genericReadCollection(Endpoints.DESIGN_STEPS, queryParams, null);
	}

}

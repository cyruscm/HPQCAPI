package com.tyson.hpqcjapi.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hpe.infrastructure.Entity;
import com.hpe.infrastructure.Entity.Fields.Field;
import com.tyson.hpqcjapi.exceptions.HPALMRestAuthException;
import com.tyson.hpqcjapi.exceptions.HPALMRestDuplicateException;
import com.tyson.hpqcjapi.exceptions.HPALMRestException;
import com.tyson.hpqcjapi.exceptions.HPALMRestMissingException;
import com.tyson.hpqcjapi.exceptions.HPALMRestPermException;
import com.tyson.hpqcjapi.exceptions.HPALMRestVCException;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.resources.Endpoints;
import com.tyson.hpqcjapi.resources.Messages;
import com.tyson.hpqcjapi.types.Entities;

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

	private Map<Class<? extends Exception>, String> errorResponseMap;

	public ALMManager() {
		super();
		initErrorResponseMap();
	}

	public void init() {
		this.validatedLogin();
		
	}
	
	private void initErrorResponseMap() {
		errorResponseMap = new HashMap<Class<? extends Exception>, String>();
		errorResponseMap.put(HPALMRestVCException.class, "qccore.check-in-failure");
		errorResponseMap.put(HPALMRestVCException.class, "qccore.check-out-failure");
		errorResponseMap.put(HPALMRestMissingException.class, "qccore.entity-not-found");
		errorResponseMap.put(HPALMRestVCException.class, "qccore.lock-failure");
		errorResponseMap.put(HPALMRestPermException.class, "qccore.operation-forbidden");
		errorResponseMap.put(HPALMRestAuthException.class, "qccore.session-has-expired");
	}

	
	// ================================================================================
	// Utilities 
	// ================================================================================

	public String URLToEndpoint(String URL) {
		return URL.replaceFirst("(http|https)://" + Config.getHost() + "/qcbin/", "");
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

	// ================================================================================
	// Generics
	// ================================================================================

	private Class<? extends Exception> getPossibleError(Object obj, Map<Class<? extends Exception>, String> responseMap) {
		if (obj == null) {
			if (lastResponse.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				return HPALMRestAuthException.class;
			} else if (lastResponse.getStatusCode() == HttpURLConnection.HTTP_CREATED) {
				return null;
			}
			
			if (responseMap != null) {
				for (Map.Entry<Class<? extends Exception>, String> entry : responseMap.entrySet()) {
					if (lastResponseContains(entry.getValue())) {
						return entry.getKey();
					}
				}
			}
			
			for (Map.Entry<Class<? extends Exception>, String> entry : errorResponseMap.entrySet()) {
				if (lastResponseContains(entry.getValue())) {
					return entry.getKey();
				}
			}
			
			return HPALMRestException.class;
		} else {
			return null;
		}
	}
	
	private void genericResponseHandler(Object entity, Map<Class<? extends Exception>, String> responseMap) throws Exception {
		Class<? extends Exception> exceptionClass = getPossibleError(entity, responseMap);
		
		if (exceptionClass == null) {
			return;
		}
		
		Exception exception = null;
		
		// Check if the exceptionClass has a constructor that takes a response. Else use default constructor
		for (Constructor<?> constructor : exceptionClass.getConstructors()) {
			Type[] paramTypes = constructor.getGenericParameterTypes();
			if (paramTypes.length == 1 && paramTypes[0] instanceof com.hpe.infrastructure.Response) {
				try {
					exception = (Exception) constructor.newInstance(lastResponse);
				} catch (Exception e) {
					Logger.logError("There was an exception in creating an exception from an exception class " + exceptionClass.getName());
					e.printStackTrace();
					System.exit(0);
				}
				continue;
			}
		}
		
		if (exception == null) {
			try {
				exception = exceptionClass.newInstance();
			} catch (Exception e) {
				Logger.logError("There was an exception in creating an exception from an exception class " + exceptionClass.getName());
				Logger.logError(e.toString());
				System.exit(0);
			}
		}
		
		throw exception;
	}

	private Entity genericCreateEntity(String endpoint, String xml, Map<Class<? extends Exception>, String> responseMap) throws Exception {
		Entity entity = createEntity(endpoint, xml);
		genericResponseHandler(entity, responseMap);
		return entity;
	}

	private Entity genericGetEntity(String endpoint, Map<Class<? extends Exception>, String> responseMap) throws Exception {
		Entity entity = readEntity(endpoint);
		genericResponseHandler(entity, responseMap);
		return entity;
	}

	private Entity genericUpdateEntity(String endpoint, String xml, Map<Class<? extends Exception>, String> responseMap) throws Exception {
		Entity entity = updateEntity(endpoint, xml);
		genericResponseHandler(entity, responseMap);
		return entity;
	}
	
	private Entity genericDeleteEntity(String endpoint, Map<Class<? extends Exception>, String> responseMap) throws Exception {
		Entity entity = deleteEntity(endpoint);
		genericResponseHandler(entity, responseMap);
		return entity;
	}

	private Entities genericReadCollection(String endpoint, Map<String, String> query,
			Map<Class<? extends Exception>, String> responseMap) throws Exception {
		Entities entities = readCollection(endpoint, query);
		genericResponseHandler(entities, responseMap);
		return entities;
	}

	// ================================================================================
	// Tests
	// ================================================================================

	public Entity createTest(String name, Map<String, String> extraParams) throws Exception {
		XMLCreator xml = new XMLCreator("Entity", "test");
		xml.addField("name", name);
		xml.addField("user-02", Config.getTeam());
		xml.addField("description", Messages.DEFAULT_DESCRIPTION(name));
		xml.addField("parent-id", Config.getUnitTestFolderID());
		xml.addField("subtype-id", "manual");

		this.appendMapToXML(xml, extraParams);
		
		return genericCreateEntity(Endpoints.TESTS, xml.publish(), null);
	}
	
	public Entity getTest(String id) throws Exception {
		return genericGetEntity(Endpoints.TEST(id), null);
	}
	
	public Entity updateTest(String id, String postedEntityXML) throws Exception {
		return genericUpdateEntity(Endpoints.TEST(id), postedEntityXML, null);
	}
	
	public Entity deleteTest(String id) throws Exception {
		return genericDeleteEntity(Endpoints.TEST(id), null);
	}
	
	public Entities getTestCollection(Map<String, String> queryParameters) throws Exception {
		return genericReadCollection(Endpoints.TESTS, queryParameters, null);
	}

	public String getTestID(String name) throws Exception {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("name", name);

		Entities entities = genericReadCollection(Endpoints.TESTS, queryParams, null);

		if (entities == null) {
			return null;
		}
		
		if (entities.Count() == 0) {
			throw new HPALMRestMissingException(lastResponse);
		} else if (entities.Count() > 1) {
			throw new HPALMRestDuplicateException(lastResponse);
		} else {
			List<Field> fields = entities.getEntities().get(0).getFields().getField();
			for (Field field : fields) {
				if (field.getName().equals("id")) {
					return field.getValue().get(0);
				}
			}
		}
		throw new HPALMRestException(lastResponse);
	}
	// ================================================================================
	// Version Control and Tools
	// ================================================================================

	public boolean checkInTest(String testID) throws Exception {
		return genericVersionControl(testID, Endpoints.CHECKIN_TEST(testID), null);
	}

	public boolean checkOutTest(String testID) throws Exception {
		return genericVersionControl(testID, Endpoints.CHECKOUT_TEST(testID), Messages.CHECKOUT_MESSAGE);
	}

	private boolean genericVersionControl(String testID, String endpoint, String comment) throws Exception {
		String data = null;

		if (comment != null) {
			XMLCreator xml = new XMLCreator("CheckOutParameters", null);
			xml.addCustomValue("Comment", comment);
			data = xml.publish();
		}

		genericCreateEntity(endpoint, data, null);
		return (lastResponse.getStatusCode() == HttpURLConnection.HTTP_CREATED);
	}

	// ================================================================================
	// Design Steps
	// ================================================================================

	public Entity createDesignStep(String name, String parentId, String description, String expected, Map<String, String> extraParams) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestVCException.class, Messages.VC_NOT_CHECKED_OUT);

		XMLCreator xml = new XMLCreator("Entity", "design-step");
		xml.addField("name", name);
		xml.addField("parent-id", parentId);
		xml.addField("description", description);
		xml.addField("expected", expected);
		
		this.appendMapToXML(xml, extraParams);

		return genericCreateEntity(Endpoints.DESIGN_STEPS, xml.publish(), responseMap);
	}

	public Entity getDesignStep(String id) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestMissingException.class, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.DESIGN_STEP(id), responseMap);
	}
	
	public Entity updateDesignStep(String id, String postedEntityXml) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestMissingException.class, Messages.DESIGN_NOT_CHECKED_OUT);
		return genericUpdateEntity(Endpoints.DESIGN_STEP(id), postedEntityXml, responseMap);
	}
	
	public Entity deleteDesignStep(String id) throws Exception {
		return genericDeleteEntity(Endpoints.DESIGN_STEP(id), null);
	}
	
	public Entities getDesignSteps(Map<String, String> queryParameters) throws Exception {
		return genericReadCollection(Endpoints.DESIGN_STEPS, queryParameters, null);
	}

	public Entities getCurrentDesignSteps(String testID) throws Exception {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("parent-id", testID);
		return genericReadCollection(Endpoints.DESIGN_STEPS, queryParams, null);
	}

	// ================================================================================
	// Test Sets
	// ================================================================================

	//TODO Find all needed vars
	public Entity createTestSet(String name, Map<String, String> extraParams) throws Exception {
		XMLCreator xml = new XMLCreator("Entity", "test-set");
		xml.addField("name", name);
		
		this.appendMapToXML(xml, extraParams);
		
		return genericCreateEntity(Endpoints.TEST_SETS, xml.publish(), null);
	}
	
	public Entity getTestSet(String id) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestMissingException.class, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.TEST_SET(id), responseMap);
	}
	
	
	public Entities queryTestSets(Map<String, String> queryParameters) throws Exception {
		return genericReadCollection(Endpoints.TEST_SETS, queryParameters, null);
	}

	public Entities queryTestSetFolders(Map<String, String> queryParameters) throws Exception {
		return genericReadCollection(Endpoints.TEST_SET_FOLDERS, queryParameters, null);
	}
	
	public String getTestSetID(String name) throws Exception {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("name", name);

		Entities entities = genericReadCollection(Endpoints.TEST_SETS, queryParams, null);

		if (entities == null) {
			return null;
		}
		
		if (entities.Count() == 0) {
			throw new HPALMRestMissingException(lastResponse);
		} else if (entities.Count() > 1) {
			throw new HPALMRestDuplicateException(lastResponse);
		} else {
			List<Field> fields = entities.getEntities().get(0).getFields().getField();
			for (Field field : fields) {
				if (field.getName().equals("id")) {
					return field.getValue().get(0);
				}
			}
		}
		throw new HPALMRestException(lastResponse);
	}

	// ================================================================================
	// Test Sets
	// ================================================================================

	public Entity createTestInstance(String testSetId, String testId, Map<String, String> extraParams) throws Exception {
		XMLCreator xml = new XMLCreator("Entity", "test-instance");
		xml.addField("cycle-id", testSetId);
		xml.addField("test-id", testId);
		xml.addField("test-order", "1");
		xml.addField("subtype-id", "hp.qc.test-instance.manual");
		
		this.appendMapToXML(xml, extraParams);
		
		return genericCreateEntity(Endpoints.TEST_INSTANCES, xml.publish(), null);
	}
	
	public Entity getTestInstance(String id) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestMissingException.class, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.TEST_INSTANCE(id), responseMap);
	}
	

	
	
	public Entities queryTestInstances(Map<String, String> queryParameters) throws Exception {
		return genericReadCollection(Endpoints.TEST_INSTANCES, queryParameters, null);
	}
	
	public String getTestInstanceId(String testSetId, String testId) throws Exception {
	Map<String, String> queryParams = new HashMap<String, String>();
	queryParams.put("cycle-id", testSetId);
	queryParams.put("test-id", testId);

	Entities entities = genericReadCollection(Endpoints.TEST_INSTANCES, queryParams, null);

	if (entities == null) {
		return null;
	}
	
	if (entities.Count() == 0) {
		throw new HPALMRestMissingException(lastResponse);
	} else if (entities.Count() > 1) {
		throw new HPALMRestDuplicateException(lastResponse);
	} else {
		List<Field> fields = entities.getEntities().get(0).getFields().getField();
		for (Field field : fields) {
			if (field.getName().equals("id")) {
				return field.getValue().get(0);
			}
		}
	}
	throw new HPALMRestException(lastResponse);
}
	
	
	// ================================================================================
	// Runs
	// ================================================================================

	public Entity createRun(String name, String testInstanceId, String testSetId, String testId) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestVCException.class, Messages.VC_NOT_CHECKED_OUT);

		XMLCreator xml = new XMLCreator("Entity", "run");
		xml.addField("test-instance", "1");
		xml.addField("name", name);
		xml.addField("owner", Config.getUsername());
		xml.addField("user-01",	Config.getUsername());
		xml.addField("testcycl-id", testInstanceId);
		xml.addField("cycle-id", testSetId);
		xml.addField("host", "HPQCJAPI");
		xml.addField("status", "Not Completed");
		xml.addField("os-name", "Junit");
		xml.addField("test-id", testId);
		xml.addField("subtype-id", "hp.qc.run.MANUAL");

		return genericCreateEntity(Endpoints.RUNS, xml.publish(), responseMap);
	}

	public Entity getRun(String id) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestMissingException.class, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.RUN(id), responseMap);
	}

	public Entity updateRun(String id, String postedEntityXml) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestVCException.class, Messages.DESIGN_NOT_CHECKED_OUT);
		return genericUpdateEntity(Endpoints.RUN(id), postedEntityXml, responseMap);
	}
	
	
	public Entity updateRunStatus(String id, String status) throws Exception {
		XMLCreator xml = new XMLCreator("Entity", "test-instance");
		xml.addField("status", status);
		
		return updateRun(id, xml.publish()); 
	}	
	public Entity deleteRun(String id) throws Exception {
		return genericDeleteEntity(Endpoints.RUN(id), null);
	}

	public Entities queryRuns(Map<String, String> queryParameters) throws Exception {
		return genericReadCollection(Endpoints.RUNS, queryParameters, null);
	}
	
	/**
	 * Gets the last run completed
	 * 
	 * Note: this is a special scenario as it should be the only time a non query-clause is used. If another one
	 * is needed at a later date, I highly suggest modifying queryCollection to support other clauses.
	 * @return Entity of latest run
	 * @throws Exception 
	 */
	public Entity getLatestRun() throws Exception {
		return getLatestMatchingRun(null);
	}

	
	public Entity getLatestMatchingRun(Map<String, String> queryParams) throws Exception {	
		String queryString = "order-by={id[desc]}?page-size=1";
		if (queryParams != null && queryParams.size() > 0) {
			StringBuilder b = new StringBuilder();
			b.append("?query=");
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				b.append("{" + entry.getKey() + "[" + entry.getValue() + "]}&");
			}
			queryString += b.toString();
		}
		Entities entities = readCollection(Endpoints.RUNS, queryString);
		genericResponseHandler(entities, null);

		return (entities.Count() > 0) ? entities.getEntities().get(0) : null;
	}

	// ================================================================================
	// Run Steps
	// ================================================================================

	public Entity createRunStep(String name, String runId, String description, String expected) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestVCException.class, Messages.VC_NOT_CHECKED_OUT);

		XMLCreator xml = new XMLCreator("Entity", "run-step");
		xml.addField("name", name);
		xml.addField("parent-id", runId);
		xml.addField("description", description);
		xml.addField("expected", expected);
		return genericCreateEntity(Endpoints.DESIGN_STEPS, xml.publish(), responseMap);
	}

	public Entity getRunStep(String runId, String id) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestMissingException.class, Messages.ENTITY_MISSING);
		return genericGetEntity(Endpoints.RUN_STEP(runId, id), responseMap);
	}

	public Entity updateRunStep(String runId, String id, String postedEntityXml) throws Exception {
		Map<Class<? extends Exception>, String> responseMap = new HashMap<Class<? extends Exception>, String>();
		responseMap.put(HPALMRestVCException.class, Messages.DESIGN_NOT_CHECKED_OUT);
		return genericUpdateEntity(Endpoints.RUN_STEP(runId, id), postedEntityXml, responseMap);
	}
	
	public Entity updateRunStepStatus(String runId, String id, String status) throws Exception {
		XMLCreator xml = new XMLCreator("Entity", "run-step");
		xml.addField("status", status);
		Entity toReturn = updateRunStep(runId, id, xml.publish());
		Logger.logDebug("Response: " + new String(lastResponse.getResponseData()));
		return toReturn;
	}
	
	public Entity deleteRunStep(String runId, String id) throws Exception {
		return genericDeleteEntity(Endpoints.RUN_STEP(runId, id), null);
	}
	
	public Entity getMatchingRunStep(String runId, String designStepId) throws Exception {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("desstep-id", designStepId);
		return genericReadCollection(Endpoints.RUN_STEPS(runId), queryParams, null).getEntities().get(0);
	}

}

package com.tyson.hpqcjapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tyson.hpqcjapi.types.Testsuites;
import com.tyson.hpqcjapi.utils.ALMManager;
import com.tyson.hpqcjapi.utils.ALMManager.Response;
import com.tyson.hpqcjapi.utils.Logger;

import infrastructure.Entity;
import infrastructure.Entity.Fields.Field;


public class JUnitPoster {
	
	private ALMManager con;
	private String name;
	private String testId;
	private Response lastCheckedResponse;
	private List<Map<String, String>> testCases;
	private Testsuites suites;
	
	public JUnitPoster(String name, JUnitReader results) {
		this.name = name;
		con = new ALMManager();
		con.init();
		suites = null;
		testCases = new ArrayList<Map<String, String>>();
	}
	
	private String getField(Entity entity, String fieldName) {
		List<Field> fields = entity.getFields().getField();
		for (Field field: fields) {
			if (field.getName().equals(fieldName)) {
				return field.getValue().get(0);
			}
		}
		return null;
	}
	
	
	public String prepareTest() {
		testId = getTestId();
		syncTestSteps();
		return testId;
	}
	
	private String getTestId() {
		String id = con.getTestID(name);
		if (id == null) {
			switch(con.getResponse()) {
			case DUPLICATE:
				Logger.logError("Duplicate tests were found. This is a fundamental error. Manual steps required. Either remove one of the duplicates or rename the test.");
				return null;
			case MISSING:
				return createTest();
			case NOAUTH:
				return null; //TODO FIXME
			case NOPERM:
				Logger.logError("The specified account does not have permission to query tests.");
				return null;
			case RETRY:
				return null; //TODO FIXME
			default:
				Logger.logError("An unexpected error occured, last response was " + con.getResponse().name());
				return null;
			}
		} else {
			return id;
		}
	}
	

	
	private String createTest() {
		Entity test = con.createTest(name, null);
		return getField(test, "id");
	}
	

	public HashMap<String, String> syncTestSteps() {
		 
		return null;
	}
	
	public String suggestedTestSet() {
		//TODO Calculate which test set to use
		return null;
	}
	
	public boolean isTestInTestSet(String testSetId, String testId) {
		//TODO Verify test is in test set
		return false;
	}
	
	public String addTestToTestSet(String testSetId, String testId) {
		return null;
	}
	
	public String createRun(String instanceId) {
		return null;
	}
	
	public void addRunSteps(String runId, HashMap<String, String> stepIdToStatus) {
		
	}
	
	

}

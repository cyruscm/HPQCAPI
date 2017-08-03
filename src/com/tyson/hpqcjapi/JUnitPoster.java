package com.tyson.hpqcjapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tyson.hpqcjapi.resources.Messages;
import com.tyson.hpqcjapi.types.Entities;
import com.tyson.hpqcjapi.types.LinkedTestCase;
import com.tyson.hpqcjapi.utils.ALMManager;
import com.tyson.hpqcjapi.utils.Logger;

import infrastructure.Entity;
import infrastructure.Entity.Fields.Field;


public class JUnitPoster {
	
	private ALMManager con;
	private String name;
	private String testId;
	private List<LinkedTestCase> cases;

	public JUnitPoster(String name, JUnitReader results) {
		this.name = name;
		con = new ALMManager();
		con.init();
		cases = results.parseSuites();
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
	
	
	
	/**
	 * Compares incoming JUnit tests to ALM tests and removes any ones no longer
	 * in the JUnit and adds new ones. Note that there is no identity tracking at this time
	 * so previous tests will be lost if the key changes. The key is the test classname + the test name
	 */
	public void syncTestSteps() {
		Entities entities = con.getCurrentDesignSteps(testId);
		List<Entity> entitiesList = new ArrayList<Entity>();
		
		entitiesList.addAll(entities.getEntities());

		List<LinkedTestCase> casesList = new ArrayList<LinkedTestCase>();
		casesList.addAll(cases);
		
		linkEqualCases(entitiesList, casesList);
	
		if (!entitiesList.isEmpty() || !casesList.isEmpty()) {
			con.checkOutTest(testId);
			if (!entitiesList.isEmpty()) {
				removeEntities(entitiesList);
			} 
			if (!casesList.isEmpty()) {
				createCaseEntities(casesList);
			}
			con.checkInTest(testId);
		}
	}
	
	/**
	 * Creates design step for each LinkedTestCase in cases and links them. Does not remove them from the list
	 * @param cases
	 */
	private void createCaseEntities(List<LinkedTestCase> cases) {
		for (LinkedTestCase testCase : cases) {
			Entity entity = con.createDesignStep(testCase.testSuite, testId, Messages.DESIGN_STEP_DESCRIPTION, testCase.getKey(), null);
			testCase.linkToEntity(entity);
		}
	}

	/**
	 * Removes each Entity in entities. Does not remove them from the list.
	 * @param entities
	 */
	private void removeEntities(List<Entity> entities) {
		for (Entity entity : entities) {
			con.deleteDesignStep(this.getField(entity, "id"));
		}
	}
	
	
	/**
	 * Removes entities and cases that are equal. Links the entities with the cases.
	 * @param firstList List of entities
	 * @param secondList List of cases to link with
	 */
	private void linkEqualCases(List<Entity> firstList, List<LinkedTestCase> secondList) {
		for (int i = 0; i < firstList.size(); i++) {
			Entity entity = firstList.get(i);
			
			for (int y = 0; y < secondList.size(); y++) {
				LinkedTestCase testCase = secondList.get(y);
				if (testCase.equals(entity)) {
					testCase.linkToEntity(entity);
					firstList.remove(i);
					secondList.remove(y);
					i--;
					break;
				}
			}
		}	
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
	
	@Override
	public String toString() {
		return cases.toString();
	}

}

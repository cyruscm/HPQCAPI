package com.tyson.hpqcjapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hpe.infrastructure.Entity;
import com.hpe.infrastructure.Entity.Fields.Field;
import com.tyson.hpqcjapi.exceptions.HPALMRestException;
import com.tyson.hpqcjapi.exceptions.HPALMRestMissingException;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.resources.Messages;
import com.tyson.hpqcjapi.types.Entities;
import com.tyson.hpqcjapi.types.LinkedTestCase;
import com.tyson.hpqcjapi.types.TestStatus;
import com.tyson.hpqcjapi.utils.ALMManager;
import com.tyson.hpqcjapi.utils.Logger;

/**
 * Provides a functional layer to HPQCJAPI that handles
 * automatically posting and adding details where necessary
 * for HPQC
 * @author MARTINCORB
 * 
 * Notes:
 * This is heavily dependent on the Config file, which must
 * be initialized before running.
 *
 */
public class JUnitPoster {

	private ALMManager con;
	private String name;
	private String testId;
	private String testSetId;
	private String testInstanceId;
	private String sumStatus;
	private String runId;

	private List<LinkedTestCase> cases;

	/**
	 * Prepares and returns JUnitPoster with name and results
	 * @param name
	 * @param results
	 */
	public JUnitPoster(String name, JUnitReader results) {
		this.name = name;
		this.testId = null;
		this.testSetId = null;
		this.testInstanceId = null;
		con = new ALMManager();
		con.init();

		cases = results.parseSuites();
		sumStatus = calcSumStatus();

		Logger.log("Determined that summed test result is \"" + sumStatus + "\"");
	}

	private String calcSumStatus() {
		boolean failedTestExists = false;

		for (LinkedTestCase testCase : cases) {
			if (testCase.status.getType().equals(TestStatus.STATUS_TYPE.FAILED)) {
				failedTestExists = true;
				break;
			}
		}

		return (failedTestExists) ? "Failed" : "Passed";
	}

	private String getField(Entity entity, String fieldName) {
		List<Field> fields = entity.getFields().getField();
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				return field.getValue().get(0);
			}
		}
		return null;
	}

	/**
	 * Posts the JUnit results to HPQC. 
	 */
	public void publishTest() {
		Logger.log("Beginning publishing JUnit results to HPQC...");
		testId = getTestId();
		Logger.logDebug("Found testID: " + testId);
		Logger.log("Synching testcases with HPQC...");
		syncTestSteps();
		
		Logger.log("Determining which test set to use...");
		testSetId = suggestedTestSet();
		Logger.logDebug("Using Test Set " + testSetId);
		
		Logger.log("Determining test instance details...");
		testInstanceId = getTestInstanceId();
		Logger.logDebug("Found testInstanceID: " + testInstanceId);
		
		Logger.log("Preparing a run for test instance...");
		runId = createRun();
		Logger.logDebug("Created run with runID:" + runId);
		
		Logger.log("Synchronizing testcase results with HPQC run");
		addRunSteps();
	}

	private String getTestId() {
		try {
			return con.getTestID(name);
		} catch (HPALMRestMissingException e) {
			Logger.log("No test matching the provided name (" + name + ") has been found. Creating one...");
			return createTest();
		} catch (Exception e) {
			return (String) handleError(e);
		}
	}

	private String createTest() {
		Entity test;
		try {
			test = con.createTest(name, null);
		} catch (Exception e) {
			return (String) handleError(e);
		}
		return getField(test, "id");
	}

	/**
	 * Compares incoming JUnit tests to ALM tests and removes any ones no longer in
	 * the JUnit and adds new ones. Note that there is no identity tracking at this
	 * time so previous tests will be lost if the key changes. The key is the test
	 * classname + the test name
	 */
	private void syncTestSteps() {
		Entities entities = null;
		try {
			entities = con.getCurrentDesignSteps(testId);
		} catch (Exception e) {
			handleError(e);
		}
		List<Entity> entitiesList = new ArrayList<Entity>();

		entitiesList.addAll(entities.getEntities());

		List<LinkedTestCase> casesList = new ArrayList<LinkedTestCase>();
		casesList.addAll(cases);

		linkEqualCases(entitiesList, casesList);

		if (!entitiesList.isEmpty() || !casesList.isEmpty()) {
			try {
				con.checkOutTest(testId);
			} catch (Exception e) {
				handleError(e);
			}
			if (!entitiesList.isEmpty()) {
				removeEntities(entitiesList);
			}
			if (!casesList.isEmpty()) {
				createCaseEntities(casesList);
			}
			try {
				con.checkInTest(testId);
			} catch (Exception e) {
				handleError(e);
			}
		}
	}

	/**
	 * Creates design step for each LinkedTestCase in cases and links them. Does not
	 * remove them from the list
	 * 
	 * @param cases
	 */
	private void createCaseEntities(List<LinkedTestCase> cases) {
		for (LinkedTestCase testCase : cases) {
			Entity entity = null;
			try {
				entity = con.createDesignStep(testCase.testSuite, testId, Messages.DESIGN_STEP_DESCRIPTION,
						testCase.getKey(), null);
			} catch (Exception e) {
				handleError(e);
			}
			testCase.linkToEntity(entity);
		}
	}

	/**
	 * Removes each Entity in entities. Does not remove them from the list.
	 * 
	 * @param entities
	 */
	private void removeEntities(List<Entity> entities) {
		for (Entity entity : entities) {
			try {
				con.deleteDesignStep(this.getField(entity, "id"));
			} catch (Exception e) {
				handleError(e);
			}
		}
	}

	/**
	 * Removes entities and cases that are equal. Links the entities with the cases.
	 * 
	 * @param firstList
	 *            List of entities
	 * @param secondList
	 *            List of cases to link with
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

	private String suggestedTestSet() {
		if (isValidString(Config.getTestSetID())) {
			if (verifyTestSetIdExists(Config.getTestSetID())) {
				return Config.getTestSetID();
			} else {
				Logger.logError(
						"Provided TestSetId of " + Config.getTestSetID() + " is not valid. Attempting remedy steps.");
			}
			return Config.getTestSetID();
		}

		if (isValidString(Config.getTestSetName())) {
			String possibleId = getTestSetIDFromName(Config.getTestSetName());
			if (possibleId != null) {
				return possibleId;
			} else {
				Logger.logError("Provided TestName of " + Config.getTestSetName()
						+ " is not valid. Attempting remedy step automatically determining test set.");
			}
		}

		if (Config.guessTestSet()) {
			Entity suggestedEntity = null;
			try {
				suggestedEntity = con.getLatestRun();
			} catch (Exception e) {
				handleError(e);
			}
			if (suggestedEntity != null) {
				return getField(suggestedEntity, "cycle-id");
			} else {
				Logger.logError(
						"No runs have been executed on the supplied ALM server, so no TesetSet can be determined");
			}
		}
		Logger.logError(
				"There is no way to get a valid TestSet with your provided paramaters. Tool could not complete. Please add a valid TestSet Name or TestSet ID. To find a valid TestSet id, open ALM and go to Test Lab then right click the desired TestSet and select Properties. The ID is located in the top left of the popup window.");
		return null;
	}

	private boolean verifyTestSetIdExists(String ID) {
		try {
			return (con.getTestSet(ID) != null);
		} catch (HPALMRestException e) {
			Logger.logDebug(new String(e.getResponse().getResponseData()));
			Logger.logDebug(new String(e.getResponse().getResponseHeaders().toString()));
			handleError(e);
			return false;
		} catch (Exception e) {
			handleError(e);
			return false;
		}
	}

	private String getTestSetIDFromName(String name) {
		try {
			return con.getTestSetID(name);
		} catch (Exception e) {
			return (String) handleError(e);
		}
	}

	private boolean isValidString(String string) {
		return (string != null && string.length() > 0);
	}

	public String getTestInstanceId() {
		Logger.logDebug("Getting test instance");
		try {
			String testInstanceId = con.getTestInstanceId(testSetId, testId);
			return testInstanceId;
		} catch (HPALMRestMissingException e) {
			return createTestInstance();
		} catch (Exception e) {
			handleError(e);
		}
		return null;
	}

	private String createTestInstance() {
		Entity test;
		Logger.logDebug("Test instance does not exist, creating");
		try {
			test = con.createTestInstance(testSetId, testId, null);
		} catch (Exception e) {
			return (String) handleError(e);
		}
		return getField(test, "id");
	}

	private String createRun() {
		String runId = null;
		try {
			runId = getField(con.createRun(name, testInstanceId, testSetId, testId), "id");
			con.updateRunStatus(runId, sumStatus);
		} catch (Exception e) {
			return (String) handleError(e);
		}
		return runId;

	}

	private void addRunSteps() {
		for (LinkedTestCase testCase : cases) {
			addRunStep(testCase);
		}
	}

	private void addRunStep(LinkedTestCase testCase) {
		try {
			Logger.logDebug("Finding Run Step for " + testCase.id);
			String matchingRunStepId = getField(con.getMatchingRunStep(runId, testCase.id), "id");
			Logger.logDebug("Updating Run Step wit status " + testCase.status.getTypeString());
			con.updateRunStepStatus(runId, matchingRunStepId, testCase.status.getTypeString(), testCase.status.getMessage());
		} catch (Exception e) {
			handleError(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(cases.toString());
		b.append("Name: " + name);
		b.append("TestSetId: " + testSetId);
		b.append("TestInstanceId: " + testInstanceId);
		b.append("sumStatus: " + sumStatus);
		return b.toString();
	}

	private Object handleError(Exception e) {
		Logger.logDebug(e.getMessage());
		return null;
	}

}

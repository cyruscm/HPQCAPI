package com.tyson.hpqcjapi;

import java.util.ArrayList;
import java.util.List;

import com.hpe.infrastructure.Entity;
import com.hpe.infrastructure.Entity.Fields.Field;
import com.tyson.hpqcjapi.exceptions.HPALMRestDuplicateException;
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
 * for HPQC. 
 * @author MARTINCORB
 * 
 * Notes:
 * This is heavily dependent on the Config file, which must
 * be initialized before running.
 */
public class JUnitPoster {

	protected ALMManager con;
	protected String sumStatus;

	protected List<LinkedTestCase> cases;

	/**
	 * Prepares and returns JUnitPoster with name and results
	 * @param name
	 * @param results
	 * @throws HPALMRestException 
	 */
	public JUnitPoster(JUnitReader results) throws HPALMRestException {
		con = new ALMManager();
		con.init();

		cases = results.parseSuites();
		sumStatus = calcSumStatus();

		Logger.log("Determined that summed test result is \"" + sumStatus + "\"");
	}

	/**
	 * Determines the testsuite status
	 * @return Failed or Passed
	 */
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
	
	public String getSumStatus() {
		return sumStatus;
	}

	/**
	 * Gets the requested Value in fieldName from entity
	 * @param entity
	 * @param fieldName
	 * @return
	 */
	protected String getField(Entity entity, String fieldName) {
		List<Field> fields = entity.getFields().getField();
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				return field.getValue().get(0);
			}
		}
		return null;
	}
	

	/**
	 * Retrieves the testId from name, or creates one if it doesn't exist.
	 * @return
	 * @throws Exception
	 * @throws IllegalArgumentException If test name is unmatched and no createFlag is set.
	 */
	private String getTestIdFromName(String name) throws Exception {
		try {
			return con.getTestID(name);
		} catch (HPALMRestMissingException e) {
			Logger.log("No test matching the provided name (" + name + ") has been found.");
			
			if (Config.createTest()) {
				Logger.log("Creating a test with name " + name);
				return createTest(name);
			} else {
				Logger.logError("You have not set --createTest flag. We cannot create a test.");
				throw(new IllegalArgumentException("No test exists with test name " + name + "and --createTest is not set." ));
			}
		}
	}
	
	
	/**
	 * Gets the testId matching Config properties.
	 * @return ID of test
	 * @throws Exception
	 * @throws IllegalArgumentException If incorrect Config properties are set.
	 */
	public String getTestId() throws Exception {
		if (isValidString(Config.getTestId())) {
			if (verifyTestIdExists(Config.getTestId())) {
				return Config.getTestId();
			} else {
				Logger.logError(
						"Provided TestId of " + Config.getTestId() + " is not valid. Attempting remedy steps.");
			}
		}

		if (isValidString(Config.getTestName())) {
			String possibleId = getTestIdFromName(Config.getTestName());
			if (possibleId != null) {
				return possibleId;
			}
		} 

		Logger.logError(
				"There is no way to get a valid Test with your provided paramaters. Tool could not complete. Please add a valid Test Name or Test ID. To find a valid Test id, open ALM and go to Test Plan then right click the desired Test and select Properties. The ID is located in the top left of the popup window.");
		throw(new IllegalArgumentException("Provided arguments of testId and/or testName were not valid"));
	}
	
	private boolean verifyTestIdExists(String id) throws Exception {
		try {
			return (con.getTest(id) != null);
		} catch (HPALMRestMissingException e) {
			return false;
		}
	}

	/**
	 * Creates a test using the provided name
	 * @return
	 * @throws Exception
	 */
	private String createTest(String name) throws Exception {
		Entity test = con.createTest(name, null);
		return getField(test, "id");
	}

	/**
	 * Compares incoming JUnit tests to ALM tests and removes any ones no longer in
	 * the JUnit and adds new ones. Note that there is no identity tracking at this
	 * time so previous tests will be lost if the key changes. The key is the test
	 * classname + the test name
	 * @param testId ID of test.
	 * @throws Exception 
	 */
	public void syncTestSteps(String testId) throws Exception {
		Entities entities = con.getCurrentDesignSteps(testId);
		List<Entity> entitiesList = new ArrayList<Entity>();

		entitiesList.addAll(entities.getEntities());

		List<LinkedTestCase> casesList = new ArrayList<LinkedTestCase>();
		casesList.addAll(cases);

		linkEqualCases(entitiesList, casesList);

		if (!entitiesList.isEmpty() || !casesList.isEmpty()) {
			con.checkOutTest(testId);
			
			removeEntities(entitiesList);
			createCaseEntities(casesList, testId);
			
			con.checkInTest(testId);
		}
	}

	/**
	 * Creates design step for each LinkedTestCase in cases and links them. Does not
	 * remove them from the list
	 * 
	 * @param cases
	 * @throws Exception 
	 */
	private void createCaseEntities(List<LinkedTestCase> cases, String testId) throws Exception {
		for (LinkedTestCase testCase : cases) {
			Entity entity = con.createDesignStep(testCase.testSuite, testId, Messages.DESIGN_STEP_DESCRIPTION,
						testCase.getKey(), null);
			testCase.linkToEntity(entity);
		}
	}

	/**
	 * Removes each Entity in entities. Does not remove them from the list.
	 * 
	 * @param entities
	 * @throws Exception 
	 */
	private void removeEntities(List<Entity> entities) throws Exception {
		for (Entity entity : entities) {
			con.deleteDesignStep(this.getField(entity, "id"));
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

	/**
	 * Returns ID of testSet to use. 
	 * First considers the directly provided id. If its invalid or does not exist, then
	 * it considers the provided TestSet name. If that is invalid, it finds the last run
	 * executed in the project, and uses its TestSet (if guessTestSet is true) 
	 * @return
	 * @throws Exception
	 */
	public String getTestSetId() throws Exception {
		
		String cTestSetId = Config.getTestSetID();
		String cTestSetName = Config.getTestSetName();
		
		if (isValidString(cTestSetId)) {
			if (verifyTestSetIdExists(cTestSetId)) {
				return cTestSetId;
			} else {
				Logger.logError("Provided TestSetId of " + cTestSetId + " is not valid. Attempting remedy steps.");
			}
		} else {
			Logger.logDebug("testSetID is not a valid string. Skipping ID steps.");
		}

		if (isValidString(cTestSetName)) {
			String possibleId = getTestSetIDFromName(cTestSetName);
			if (possibleId != null) {
				return possibleId;
			} else {
				Logger.logError("Provided TestName of " + cTestSetName
						+ " is not valid. Attempting remedy step automatically determining test set.");
			}
		} else {
			Logger.logDebug("testSetName is not a valid string. Skipping name steps.");
		}

		if (Config.guessTestSet()) {
			Entity suggestedEntity = con.getLatestRun();
			
			if (suggestedEntity != null) {
				return getField(suggestedEntity, "cycle-id");
			} else {
				Logger.logError(
						"No runs have been executed on the supplied ALM server, so no TesetSet can be determined");
			}
		} else {
			Logger.log("You did not set guessTestSet to true, cannot automatically find a test set.");
		}
		Logger.logError(
				"There is no way to get a valid TestSet with your provided paramaters. Tool could not complete. Please add a valid TestSet Name or TestSet ID. To find a valid TestSet id, open ALM and go to Test Lab then right click the desired TestSet and select Properties. The ID is located in the top left of the popup window.");
		throw(new IllegalArgumentException("Provided TestSetId and/or TestSetName were not valid."));
	}

	
	/**
	 * Checks if a TestSet ID is valid
	 * @param ID TestSet ID
	 * @return True if exists, false if not
	 * @throws Exception
	 */
	private boolean verifyTestSetIdExists(String ID) throws Exception {
		try {
			return (con.getTestSet(ID) != null);
		} catch (HPALMRestMissingException e) {
			return false;
		}
	}

	/**
	 * Gets testSetID from name...
	 * @param name Name of test set to search for
	 * @return ID if found, null if not
	 * @throws Exception
	 */
	private String getTestSetIDFromName(String name) throws Exception {
		try {
			return con.getTestSetID(name);
		} catch (HPALMRestMissingException e) {
			Logger.logError("No TestSets matching " + name + " were found.");
			return null;
		} catch (HPALMRestDuplicateException e) {
			Logger.logError("Duplicate TestSets were found matching " + name + ".");
			return null;
		}
	}

	/**
	 * Checks if a string is not null and not empty
	 * @param string
	 * @return
	 */
	private boolean isValidString(String string) {
		return (string != null && string.length() > 0);
	}

	
	/**
	 * Gets or creates an instance matching testId in the matching testSetId
	 * @param testId ID of Test
	 * @param testSetId ID of TestSet
	 * @return Instance ID
	 * @throws Exception
	 */
	public String getTestInstanceId(String testId, String testSetId) throws Exception {
		Logger.logDebug("Getting test instance");
		try {
			String testInstanceId = con.getTestInstanceId(testSetId, testId);
			return testInstanceId;
		} catch (HPALMRestMissingException e) {
			return createTestInstance(testId, testSetId);
		} 
	}
	
	private boolean verifyTestInstanceExists(String id) throws Exception {
		try {
			return (con.getTestInstance(id) != null);
		} catch (HPALMRestMissingException e) {
			return false;
		}
	}

	/**
	 * Creates a TestInstance for test matching testId in the TestSet matching testSetId
	 * @param testId ID of test
	 * @param testSetId ID of TestSet to use
	 * @return ID of new testInstance
	 * @throws Exception
	 */
	private String createTestInstance(String testId, String testSetId) throws Exception {

		Logger.logDebug("Test instance does not exist, creating");

		if (!verifyTestSetIdExists(testSetId)) {
			throw(new IllegalArgumentException("Provided TestSetId of " + testSetId +" is not valid"));
		} else if (!verifyTestIdExists(testId)) {
			throw(new IllegalArgumentException("Provided TestID of " + testId + " is not valid."));
		}
		
		Entity test = con.createTestInstance(testSetId, testId, null);

		return getField(test, "id");
	}

	/**
	 * Creates a run for the given instanceId
	 * @param name Name of the run
	 * @param testId ID of test
	 * @param testSetId ID of TestSet
	 * @param testInstanceId ID of testInstance
	 * @return ID of run
	 * @throws Exception
	 */
	public String createRun(String name, String testId, String testSetId, String testInstanceId) throws Exception {
		if (!verifyTestSetIdExists(testSetId)) {
			throw(new IllegalArgumentException("Provided TestSetId of " + testSetId +" is not valid"));
		} else if (!verifyTestIdExists(testId)) {
			throw(new IllegalArgumentException("Provided TestID of " + testId + " is not valid."));
		} else if (!verifyTestInstanceExists(testInstanceId)) {
			throw(new IllegalArgumentException("Provided TestInstanceId of " + testInstanceId + " is not valid."));
		}
		
		String runId = getField(con.createRun(name, testInstanceId, testSetId, testId), "id");
		// See here https://community.saas.hpe.com/t5/Quality-Center-ALM-Practitioners/ALM-REST-API-Updating-Test-Instance-Status-without-creating-a/td-p/983522
		// for why we are having to manually update the Run status instead of passing it in creation
		con.updateRunStatus(runId, sumStatus);
		return runId;

	}

	/**
	 * Syncs the results from JUnit to run step
	 * @param runId
	 * @throws Exception
	 */
	public void syncRunSteps(String runId) throws Exception {
		for (LinkedTestCase testCase : cases) {
			addRunStep(testCase, runId);
		}
	}

	/**
	 * Posts the testCases information to the matching runStep
	 * @param testCase
	 * @param runId
	 * @throws Exception
	 */
	private void addRunStep(LinkedTestCase testCase, String runId) throws Exception {
		String matchingRunStepId = getField(con.getMatchingRunStep(runId, testCase.id), "id");
		testCase.runStepId = matchingRunStepId;
		con.updateRunStepStatus(runId, matchingRunStepId, testCase.status.getTypeString(), testCase.status.getMessage());
	}
	
	/**
	 * Closes connection if one is active and logs out of alm. 
	 */
	public void close() {
		con.logout();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(cases.toString());
		b.append("sumStatus: " + sumStatus);
		return b.toString();
	}
}

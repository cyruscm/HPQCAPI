/**
 * 
 */
package test.java;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tyson.hpqcjapi.HPQCJAPI;
import com.tyson.hpqcjapi.JUnitReader;
import com.tyson.hpqcjapi.exceptions.HPALMRestException;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.types.LinkedTestCase;
import com.tyson.hpqcjapi.utils.Logger;

/**
 * @author MARTINCORB
 *
 */
public class JUnitPosterTest{

	private static String password = "";
	private static String username = "";
	private static String ARG_STRING = "-d PROJECTS -D Big_Data_API -v -s 103 -u %s -p %s %s sampleJunit.xml";
	private static final String TESTSETID = "103";
	private static final String HPQCJAPIFEATURETESTID = "45";
	private static final String TESTINSTANCEID = "37";
	private InputStream SAMPLEJUNIT = getClass().getResourceAsStream("resources/sampleJunit.xml");
	private InputStream UPDATEDJUNIT = getClass().getResourceAsStream("resources/updatedJunit.xml");
	
	/**
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws JAXBException 
	 * @throws HPALMRestException 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpClass() throws IOException, ParseException, HPALMRestException, JAXBException {
		try {
			Path passFile = Paths.get("passfile.txt");
			if (Files.exists(passFile)) {
				List<String> passLines = Files.readAllLines((Paths.get("passfile.txt")));
				if (passLines.size() == 2) {
					username = passLines.get(0);
					password = passLines.get(1);
					return;
				} else {
					Logger.logError("A passfile.txt is expected with only 2 lines. The first line is a username, the second is a password.");
				}
			} 
			
			username = System.getProperty("testUser");
			password = System.getProperty("testPass");
			Logger.logWarning("USER: " + username + " -- Password: " + password);
			
		} catch (IOException e) {
			Logger.logError("A passfile.txt is expected to run tests. This has a username on the first line and password on the second line.");
			throw(e);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownClass() throws Exception {
		username = "";
		password = "";
	}
	
	/**
	 * Test method for {@link com.tyson.hpqcjapi.TestingJUnitPoster#TestingJUnitPoster(com.tyson.hpqcjapi.JUnitReader)}.
	 * @throws JAXBException 
	 * @throws HPALMRestException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@Test
	public void testTestingJUnitPoster() throws ParseException, IOException, HPALMRestException, JAXBException {
		if (HPQCJAPI.prepareSteps(String.format(ARG_STRING, username, password, "-n TestHPQCJAPIFeatures").split("\\s+"))) {
			TestingJUnitPoster poster = new TestingJUnitPoster(new JUnitReader(SAMPLEJUNIT));
			assertEquals("TestingJUnitPoster did not properly calculate the sum status", "Failed", poster.getSumStatus());
			poster = new TestingJUnitPoster(new JUnitReader(UPDATEDJUNIT));
			assertEquals("TestingJUnitPoster did not properly calculate the sum status", "Passed", poster.getSumStatus());
			
			
		} else {
			throw(new IllegalArgumentException("Config preperation resulted in error."));
		}
	}

	/**
	 * Test method for {@link com.tyson.hpqcjapi.TestingJUnitPoster#getTestId()}.
	 * @throws Exception 
	 */
	@Test
	public void testGetTestId() throws Exception {
		if (HPQCJAPI.prepareSteps(String.format(ARG_STRING, username, password, "-n TestHPQCJAPIFeatures").split("\\s+"))) {
			TestingJUnitPoster poster = new TestingJUnitPoster(new JUnitReader(SAMPLEJUNIT));
			assertEquals("Poster did not validate testId with only name", HPQCJAPIFEATURETESTID, poster.getTestId());
			
			Config.overrideProperty("testId", "45");
			assertEquals("Poster did not validate testId with id and name", HPQCJAPIFEATURETESTID, poster.getTestId());
			
			Config.overrideProperty("testName", "");
			assertEquals("Poster did not validate testId with only id", HPQCJAPIFEATURETESTID, poster.getTestId());
			
			Config.overrideProperty("testId", "");
			try {
				poster.getTestId();
				fail("Poster did not throw IllegalArgumentException with no id or name");
			} catch (IllegalArgumentException e) {
			}
			
			Config.overrideProperty("testName", "NonExistantTest!");
			try {
				poster.getTestId();
				fail("Poster did not throw IllegalArgumentException with unmatching name and no createFlag");
			} catch (IllegalArgumentException e) {
			}			

			//TODO Find a way to test create without spamming new tests on every run. AKA get delete perms
			// alternatively a method like ALMManager#getLatestRun() can be used with a prefix + wildcard
			// and and use prefix + increment for tests.
			
		} else {
			throw(new IllegalArgumentException("Config preperation resulted in error."));
		}
	}

	/**
	 * Test method for {@link com.tyson.hpqcjapi.TestingJUnitPoster#getTestSetId()}.
	 * @throws Exception 
	 */
	@Test
	public void testGetTestSetId() throws Exception {
		if (HPQCJAPI.prepareSteps(String.format(ARG_STRING, username, password, "-n TestHPQCJAPIFeatures -S HPQCJAPI_Unit_Tests").split("\\s+"))) {
			TestingJUnitPoster poster = new TestingJUnitPoster(new JUnitReader(SAMPLEJUNIT));
			assertEquals("Poster did not validate testSetId with only name", TESTSETID, poster.getTestSetId());
			
			Config.overrideProperty("testSetId", "103");
			assertEquals("Poster did not validate testId with id and name", TESTSETID, poster.getTestSetId());
			
			Config.overrideProperty("testSetName", "");
			assertEquals("Poster did not validate testId with only id", TESTSETID, poster.getTestSetId());
			
			Config.overrideProperty("testSetId", "");
			try {
				poster.getTestSetId();
				fail("Poster did not throw IllegalArgumentException with no id or name");
			} catch (IllegalArgumentException e) {
			}
			
			Config.overrideProperty("testSetName", "NonExistantTest!");
			try {
				poster.getTestSetId();
				fail("Poster did not throw IllegalArgumentException with unmatching name");
			} catch (IllegalArgumentException e) {
			}	
		} else {
			throw(new IllegalArgumentException("Config preperation resulted in error."));
		}
	}

	/**
	 * Test method for {@link com.tyson.hpqcjapi.TestingJUnitPoster#getTestInstanceId(java.lang.String, java.lang.String)}.
	 * @throws Exception 
	 * @throws JAXBException 
	 * @throws HPALMRestException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@Test
	public void testGetTestInstanceId() throws ParseException, IOException, HPALMRestException, JAXBException, Exception {
		if (HPQCJAPI.prepareSteps(String.format(ARG_STRING, username, password, "-n TestHPQCJAPIFeatures -S HPQCJAPI_Unit_Tests").split("\\s+"))) {
			TestingJUnitPoster poster = new TestingJUnitPoster(new JUnitReader(SAMPLEJUNIT));
			
			assertEquals("Poster did not get correct testInstanceId", TESTINSTANCEID, poster.getTestInstanceId(HPQCJAPIFEATURETESTID, TESTSETID));
			
			//TODO They seriously need to give delete permisisons...
			
			/**
			String createDeleteId = poster.getTestInstanceId(CREATEDELETETESTID, TESTSETID);
			assertTrue("Poster did not create TestInstance", poster.testInstanceExists(createDeleteId));
			poster.deleteInstance(createDeleteId); **/
			
			try {
				poster.getTestInstanceId("" + Integer.MAX_VALUE, TESTSETID);
				fail("Invalid TestId did not result in an error");
			} catch (IllegalArgumentException e) {
			}
			
			try {
				poster.getTestInstanceId(HPQCJAPIFEATURETESTID, "" + Integer.MAX_VALUE);
				fail("Invalid TestSetId did not result in an error");
			} catch (IllegalArgumentException e) {
			}
			
		} else {
			throw(new IllegalArgumentException("Config preperation resulted in error."));
		}		
	}

	/**
	 * Test method for {@link com.tyson.hpqcjapi.TestingJUnitPoster#createRun(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testCreateAndSyncRun() throws Exception {
		if (HPQCJAPI.prepareSteps(String.format(ARG_STRING, username, password, "-n TestHPQCJAPIFeatures -S HPQCJAPI_Unit_Tests").split("\\s+"))) {
			JUnitReader reader = new JUnitReader(SAMPLEJUNIT);
			TestingJUnitPoster poster = new TestingJUnitPoster(reader);
			String runId = poster.createRun("UnitTestMetaRun", HPQCJAPIFEATURETESTID, TESTSETID, TESTINSTANCEID);
			assertEquals("createRun did not set status properly for Failure", poster.getSumStatus(), poster.getRunStatus(runId));
			poster.syncTestSteps(HPQCJAPIFEATURETESTID);
			poster.syncRunSteps(runId);
			runStepsMatchStatus(poster, reader, runId);
			
			reader = new JUnitReader(UPDATEDJUNIT);
			poster = new TestingJUnitPoster(reader);
			runId = poster.createRun("UnitTestMetaRun", HPQCJAPIFEATURETESTID, TESTSETID, TESTINSTANCEID);
			assertEquals("createRun did not set status properly for Success", poster.getSumStatus(), poster.getRunStatus(runId));
			poster.syncTestSteps(HPQCJAPIFEATURETESTID);
			poster.syncRunSteps(runId);
			runStepsMatchStatus(poster, reader, runId);
			
			try {
				poster.createRun("If_You_See_Me_I_Failed", "" + Integer.MAX_VALUE, TESTSETID, TESTINSTANCEID);
				fail("Invalid TestId did not result in an error");
			} catch (IllegalArgumentException e) {
			}
			
			try {
				poster.createRun("If_You_See_Me_I_Failed", HPQCJAPIFEATURETESTID, "" + Integer.MAX_VALUE, TESTINSTANCEID);
				fail("Invalid TestSetId did not result in an error");
			} catch (IllegalArgumentException e) {
			}
			
			try {
				poster.createRun("If_You_See_Me_I_Failed", HPQCJAPIFEATURETESTID, TESTSETID, "" + Integer.MAX_VALUE);
				fail("Invalid TestInstanceId did not result in an error");
			} catch (IllegalArgumentException e) {
			}
			
			
		} else {
			throw(new IllegalArgumentException("Config preperation resulted in error."));
		}
	}
	
	private void runStepsMatchStatus(TestingJUnitPoster poster, JUnitReader reader, String runId) throws Exception {
		for (LinkedTestCase testCase : poster.getCases()) {
			assertEquals("Run step " + testCase.id + " did not update status properly.", testCase.status.getTypeString(), poster.getRunStepStatus(runId, testCase.runStepId)); 
		}
	}

}

package com.tyson.hpqcjapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tyson.hpqcjapi.HPQCJAPI;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.resources.Constants;
import com.tyson.hpqcjapi.utils.Logger;

public class HPQCJAPITest {

	//private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	//private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    //System.setOut(new PrintStream(outContent));
	    //System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	   //System.setOut(null);
	    //System.setErr(null);
	}
	
	@Test
	public void ConfigVerifiesContent() {
		Map<String[], Boolean> testStrings = new HashMap<String[], Boolean>();
		
		testStrings.put("-u User -p Pass -S name".split("\\s+"), false);
		testStrings.put("-t -u -p".split("\\s+"), false);
		testStrings.put("-T".split("\\s+"), false);
		testStrings.put("-u".split("\\s+"), false);
		testStrings.put("-u JUnitOuput.xml".split("\\s+"), false);
		testStrings.put("-u martincorb -p True007 -n HPQCJAPI-Diff-Test -v sampleJunit.xml".split("\\s+"), true);
						 
		String str = "Failure: I was expecting arg input %s to return %b but it didn't.";
		                        		
		for (Map.Entry<String[], Boolean> entry : testStrings.entrySet()) {
			try {
				str = "Failure: I was expecting arg input %s to return %b but it didn't.";
				HPQCJAPI.valid_input = true;
				assertTrue(String.format(str, utils.joinStringArray(entry.getKey()), entry.getValue()) + "\n"  /** outContent.toString() **/, HPQCJAPI.prepareSteps(entry.getKey()) == entry.getValue());
			} catch (Exception e) {
				fail(utils.getStackStrace(e));
			}
		}
	}
	
	
	@Test
	public void ConfigSetsParams() {
		HPQCJAPI.valid_input = true;
		try {
			HPQCJAPI.prepareSteps("-u User -p Pass -S Name -s ID -H Host -P Port -d Domain -D Project -T Team -t ID -f FolderId -n TestName -g -c -v".split("\\s+"));
		} catch (Exception e) {
			fail(utils.getStackStrace(e));
		} 

		assertEquals("User", Config.getUsername());
		assertEquals("Pass", Config.getPassword());
		assertEquals("Name", Config.getTestSetName());
		assertEquals("ID", Config.getTestSetID());
		assertEquals("Host", Config.getHost());
		assertEquals("Port", Config.getPort());
		assertEquals("Domain", Config.getDomain());
		assertEquals("Project", Config.getProject());
		assertEquals("Team", Config.getTeam());
		assertEquals("FolderId", Config.getUnitTestFolderID());
		assertEquals("TestName", Config.getTestName());
		assertEquals("ID", Config.getTestId());
		assertTrue("Guess test set flag was not set properly.", Config.guessTestSet());
		assertTrue("Create Test flag was not set propertly.", Config.createTest());
		assertTrue("Verbose flag was not set propertly.", Config.verbose());
	}
	

	@Test
	public void ConfigDefaultsParams() {
		try {
			Config.reset();
		} catch (IOException e) {
			fail(utils.getStackStrace(e));
		}
		
		assertEquals(Constants.USERNAME, Config.getUsername());
		assertEquals(Constants.PASSWORD, Config.getPassword());
		assertEquals(Constants.TESTSETNAME, Config.getTestSetName());
		assertEquals(Constants.TESTSETID, Config.getTestSetID());
		assertEquals(Constants.HOST, Config.getHost());
		assertEquals(Constants.PORT, Config.getPort());
		assertEquals(Constants.DOMAIN, Config.getDomain());
		assertEquals(Constants.PROJECT, Config.getProject());
		assertEquals(Constants.TEAM, Config.getTeam());
		assertEquals(Constants.FOLDERID, Config.getUnitTestFolderID());
		assertEquals(Constants.TESTNAME, Config.getTestName());
		assertEquals(Constants.TESTID, Config.getTestId());
		assertTrue("Guess test set flag was not set properly." , !Config.guessTestSet());
		assertTrue("Create Test flag was not set propertly.", !Config.createTest());
		assertTrue("Verbose flag was not set properly.", !Config.verbose());
	}
	
}

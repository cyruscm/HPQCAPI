package com.tyson.hpqcjapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.hpe.infrastructure.EntityMarshallingUtils;
import com.tyson.hpqcjapi.types.Failure;
import com.tyson.hpqcjapi.types.LinkedTestCase;
import com.tyson.hpqcjapi.types.TestStatus;
import com.tyson.hpqcjapi.types.Testcase;
import com.tyson.hpqcjapi.types.Testsuite;
import com.tyson.hpqcjapi.types.Testsuites;
import com.tyson.hpqcjapi.utils.Logger;

/**
 * Parses a default JUnit XML file. 
 * @author MARTINCORB
 *
 */
public class JUnitReader {

	private Testsuites suites;
	private String path;

	/**
	 * Create a JUnit parser from the xml file path
	 * @param path Path to file. Relative from run or absolute. Include file ending.
	 * @throws IOException 
	 * @throws JAXBException 
	 */
	public JUnitReader(String path) throws IOException, JAXBException {
		this.path = path;
		String xml = new String(Files.readAllBytes(Paths.get(path)));
		suites = EntityMarshallingUtils.marshal(Testsuites.class, xml);
	}

	/**
	 * Output a list of List of LinkedTestCases parsed form inputted xml
	 * @return
	 */
	public List<LinkedTestCase> parseSuites() {
		Logger.log("Parsing JUnit xml at " + Paths.get(path).toAbsolutePath());
		List<LinkedTestCase> cases = new ArrayList<LinkedTestCase>();
		for (Testsuite suite : suites.getTestsuite()) {
			for (Testcase testcase : suite.getTestcase()) {
				LinkedTestCase newcase = new LinkedTestCase(suite.getName(), testcase.getClassname(),
						testcase.getName(), getTestStatus(testcase));
				verifiedDetailAppend(testcase, newcase);

				cases.add(newcase);
			}
		}
		return cases;
	}

	/**
	 * Combine list of strings with new lines
	 * @param strings
	 * @return
	 */
	private String concatStrings(List<String> strings) {
		StringBuilder b = new StringBuilder();
		for (String line : strings) {
			b.append(line + "\n");
		}
		return b.toString();
	}

	/**
	 * Appends excess data to a LinkedTestCase that is optional
	 * @param testcase
	 * @param newcase
	 */
	private void verifiedDetailAppend(Testcase testcase, LinkedTestCase newcase) {
		if (!testcase.getSystemErr().isEmpty()) {
			newcase.systemErr = concatStrings(testcase.getSystemErr());
		}

		if (!testcase.getSystemOut().isEmpty()) {
			newcase.systemOut = concatStrings(testcase.getSystemOut());
		}

		if (testcase.getTime() != null) {
			newcase.time = testcase.getTime();
		}
	}

	/**
	 * Turns a Testcase status into a TestStatus object with messages and types
	 * @param testcase
	 * @return
	 */
	private TestStatus getTestStatus(Testcase testcase) {
		if (testcase.getSkipped() != null && !testcase.getSkipped().isEmpty()) {
			return new TestStatus(TestStatus.STATUS_TYPE.SKIPPED, testcase.getSkipped());
		} else if (testcase.getFailure() != null && !testcase.getFailure().isEmpty()) {
			StringBuilder b = new StringBuilder();
			for (Failure failure : testcase.getFailure()) {
				b.append(failure.getMessage() + ": \n");
				b.append(failure.getContent() + "\n");
			}

			return new TestStatus(TestStatus.STATUS_TYPE.FAILED, b.toString());
		} else {
			return new TestStatus(TestStatus.STATUS_TYPE.PASSED, "No failures found.");
		}
	}
}

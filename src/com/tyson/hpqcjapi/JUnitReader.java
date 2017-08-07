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

public class JUnitReader {

	private Testsuites suites;
	
	public JUnitReader(String path) {
		try {
			String xml = new String(Files.readAllBytes(Paths.get(path)));
			suites = EntityMarshallingUtils.marshal(Testsuites.class, xml);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public List<LinkedTestCase> parseSuites() {
		List<LinkedTestCase> cases = new ArrayList<LinkedTestCase>(); 
		for (Testsuite suite : suites.getTestsuite()) {
			for (Testcase testcase : suite.getTestcase()) {
				LinkedTestCase newcase = new LinkedTestCase(
						suite.getName(),
						testcase.getClassname(),
						testcase.getName(),
						getTestStatus(testcase)
				);
				verifiedDetailAppend(testcase, newcase);
				
				cases.add(newcase);
			}
		}
		return cases;
	}
	
	private String concatStrings(List<String> strings) {
		StringBuilder b = new StringBuilder();
		for (String line : strings) {
			b.append(line + "\n");
		}
		return b.toString();
	}
	
	
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
	
	
	
	private TestStatus getTestStatus(Testcase testcase) {
		if (testcase.getSkipped() != null && !testcase.getSkipped().isEmpty()) {
			return new TestStatus(TestStatus.STATUS_TYPE.SKIPPED, 
					testcase.getSkipped());
		} else if (testcase.getFailure() != null && !testcase.getFailure().isEmpty()) {
			StringBuilder b = new StringBuilder();
			for (Failure failure: testcase.getFailure()) {
				b.append(failure.getMessage() + "\n");
			}
			
			return new TestStatus(TestStatus.STATUS_TYPE.FAILED, b.toString());
		} else {
			return new TestStatus(TestStatus.STATUS_TYPE.PASSED, "");
		}
	}
}

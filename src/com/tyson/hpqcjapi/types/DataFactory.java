package com.tyson.hpqcjapi.types;

import com.tyson.hpqcjapi.utils.Logger;

public class DataFactory {

	public LinkedTestCase createLinkedTestCase(String testSuite, String className, String name, TestStatus status) {
		if (testSuite == null || testSuite.length() == 0) {
			Logger.logError("LinkedTestCase must have a value for testSuite.");
			return null;
		} else if (className == null || className.length() == 0) {
			Logger.logError("LinkedTestCase must have a value for className.");
			return null;
		} else if (name == null || name.length() == 0) {
			Logger.logError("LinkedTestCase must have a value for name.");
			return null;
		} else if (status == null || status.getType() == null) {
			Logger.logError("LinkedTestCase must have a StatusType.");
		}

		return new LinkedTestCase(testSuite, className, name, status);
	}

}

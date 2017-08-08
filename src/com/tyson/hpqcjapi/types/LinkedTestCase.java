package com.tyson.hpqcjapi.types;

import org.jsoup.Jsoup;

import com.hpe.infrastructure.Entity;
import com.hpe.infrastructure.Entity.Fields.Field;
import com.tyson.hpqcjapi.utils.Logger;

/**
 * This holds information about a test case that is shared between ALM and JUnit
 * 
 * @author MARTINCORB 
 * 
 * INVARIANTS: 1. TestCases are compared by 3 key values.
 *         testSuite must be the same. Additionally, the key must be the same
 *         (getKey()), which is the className and name combined.
 * 
 *         2. All values except testSuite, className, name, and Status can be
 *         null. 3. id and testId are associated with ALM information, and must
 *         be dynamically populated 4. systemOut, SystemErr, and time are
 *         optional, as defined by XSD standards of JUnit.
 */
public class LinkedTestCase {

	public String testSuite;
	public String className;
	public String name;
	public String id;
	public String testId;
	public TestStatus status;
	public String systemOut;
	public String systemErr;
	public String time;

	public LinkedTestCase(String testSuite, String className, String name, TestStatus status) {
		this.testSuite = testSuite;
		this.className = className;
		this.name = name;
		this.status = status;
	}

	public boolean equals(Object obj) {
		if (obj.getClass() != LinkedTestCase.class) {
			return false;
		}
		LinkedTestCase mappedObj = (LinkedTestCase) obj;
		if (mappedObj.testSuite == this.testSuite && mappedObj.getKey().equals(this.getKey())) {
			return true;
		}
		return false;
	}

	public void linkToEntity(Entity entity) {
		if (!(entity.getType().equals("design-step"))) {
			Logger.logError(
					"Requested entity for linkToEntity on LinkedTestCase " + this.getKey() + " is not a design-step");
			return;
		}

		String id = null;
		String testId = null;

		for (Field field : entity.getFields().getField()) {
			if (id != null && testId != null) {
				break;
			} else if (field.getName().equals("id")) {
				id = field.getValue().get(0);
			} else if (field.getName().equals("parent-id")) {
				testId = field.getValue().get(0);
			}
		}

		if (testId == null) {
			Logger.logError("Requested entity for linkToEntity on LinkedTestCase " + this.getKey()
					+ " does not have a parent-id?");
		} else {
			this.testId = testId;
		}

		if (id == null) {
			Logger.logError(
					"Requested entity for linkToEntity on LinkedTestCase " + this.getKey() + " does not have a id?");
		} else {
			this.id = id;
		}

	}

	public boolean equals(Entity entity) {
		if (!(entity.getType().equals("design-step"))) {
			return false;
		}

		String expected = null;
		String name = null;

		for (Field field : entity.getFields().getField()) {
			if (expected != null && name != null) {
				break;
			} else if (field.getName().equals("expected")) {
				expected = field.getValue().get(0);
			} else if (field.getName().equals("name")) {
				name = field.getValue().get(0);
			}
		}

		if (name.equals(testSuite) && Jsoup.parse(expected).text().equals(this.getKey())) {
			return true;
		}

		return false;
	}

	public String getKey() {
		return className + ": " + name;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[key: " + this.getKey());
		b.append(", testSuite: " + this.testSuite);
		b.append(", id: " + id);
		b.append(", statusMessage; " + this.status.getMessage());
		b.append(", parent-id: " + testId + "]");
		return b.toString();
	}

}

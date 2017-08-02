package com.tyson.hpqcjapi.types;

public class LinkedTestCase {
	

	
	public String testSuite;
	public String classname;
	public String name;
	public String id;
	public String testId;
	public TestStatus status;
	public String systemOut;
	public String systemErr;
	public String time;
	
	public LinkedTestCase(String testSuite, String classname, String name, TestStatus status) {
		this.testSuite = testSuite;
		this.classname = classname;
		this.name = name;
		this.status = status;
	}
	
}


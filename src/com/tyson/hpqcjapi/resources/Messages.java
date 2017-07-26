package com.tyson.hpqcjapi.resources;

public class Messages {
	
	public static final String TEST_ALREADY_EXISTS = "Duplicate test name ";
	public static final String SUBTYPE_ID = "MANUAL";
	public static final String CHECKOUT_MESSAGE = "Automatically adding test parameters";
	public static final String DEFAULT_DESCRIPTION(String name) { 
		return "Automatically generated test for symbolic JUnit on program " + name;
	}

}

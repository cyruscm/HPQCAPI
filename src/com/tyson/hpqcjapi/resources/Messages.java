package com.tyson.hpqcjapi.resources;

public class Messages {

	public static final String TEST_ALREADY_EXISTS = "Duplicate test name ";
	public static final String ENTITY_MISSING = "No entity of type ";
	public static final String DESIGN_NOT_CHECKED_OUT = "Failed to post";
	public static final String VC_NOT_CHECKED_OUT = "Owner entity is not checked out by ";

	public static final String SUBTYPE_ID = "MANUAL";
	public static final String CHECKOUT_MESSAGE = "Automatically adding test parameters";

	public static final String INCORRECT_RESPONSE_CODE(String name, String response, String expected) {
		return name + " did not return " + expected + ", instead received " + response;
	}

	public static final String UNEXPECTED_ERROR(String name, String endpoint) {
		return name + " had an unexpected error at " + endpoint;
	}

	public static final String DEFAULT_DESCRIPTION(String name) {
		return "Automatically generated test for symbolic JUnit on program " + name;
	}

	public static final String[] EXPECTED_SITE_SESSION_COOKIES = { "XSRF-TOKEN", "ALM_USER", "JSESSIONID",
			"QCSession" };

	public static final String DESIGN_STEP_DESCRIPTION = "This is a symbolic step that should never be manually run. It is ran by an automated system.";

}

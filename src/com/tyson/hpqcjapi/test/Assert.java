package com.tyson.hpqcjapi.test;

import com.tyson.hpqcjapi.utils.Logger;

/**
 * Testing package for asserts
 * Created by MARTINCORB on 7/14/2017.
 */
public class Assert {

	public static boolean throwErrors = true;
	
    static int tests = 0;
    static int passed = 0;

    public static final void assertTrue (final String errorMessage,
           boolean assertee){
        tests++;
        if (!assertee) {
            logError(errorMessage, new RuntimeException(errorMessage));
        } else {
            passed++;
        }
    }

    public static final void assertEquals (final String errorMessage,
            final String expressionOne, final String expressionTwo) {
        tests++;
        if (!expressionOne.equals(expressionTwo)) {
            logError(errorMessage, new RuntimeException(errorMessage));
        } else {
            passed++;
        }
    }

    public static void assertEquals(String errorMessage,
           int expressionOne, int expressionTwo) {
        tests++;
        if (expressionOne != expressionTwo) {
            logError(errorMessage, new RuntimeException(errorMessage));
        } else {
            passed++;
        }
    }

    public static void assertNull(String errorMessage, String assertee) {
        tests++;
        if (assertee != null) {
             logError(errorMessage, new RuntimeException(errorMessage));
        } else {
            passed++;
        }
    }

    public static void assertNotNull(String errorMessage,
            String assertee) {
        tests++;
        if (assertee == null) {
             logError(errorMessage, new RuntimeException(errorMessage));
        } else {
            passed++;
        }
    }


    private static void logError(String errorMessage, RuntimeException e) throws RuntimeException {
    	if (throwErrors) {
    		throw(e);
    	} else {
    		Logger.logError(errorMessage);
    		Logger.logError(e.getMessage());
    	}
    }

    public static void publishResults() {
        System.out.println("     TEST RESULTS     ");
        System.out.println("======================");
        System.out.println("Total Tests: " + tests);
        System.out.println("Total Passed: " + passed);
        System.out.println("Total Failed: " + (tests - passed));
        System.out.println("Pass Rate: " + ((passed / tests) * 100) + "%");


    }
}

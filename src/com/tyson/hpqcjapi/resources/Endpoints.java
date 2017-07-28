package com.tyson.hpqcjapi.resources;

/**
 * Created by MARTINCORB on 7/14/2017.
 */
public class Endpoints {
    private static final String BASE_ENDPOINT = String.format("rest/domain/%s/projects/%s/", Config.getDomain(), Config.getProject());

    public static final String AUTHENTICATE = "authentication-point/authenticate";
    public static final String LOGOUT = "authentication-point/logout";
    public static final String IS_AUTHENTICATED = "rest/is-authenticated";
    public static final String SITE_SESSION = "rest/site-session";
    
    public static final String DESIGN_STEPS = BASE_ENDPOINT + "run-steps";
    public static final String DESIGN_STEP(String id) { return DESIGN_STEPS + "/" + id;}
    
    public static final String TESTS = BASE_ENDPOINT + "tests";
    public static final String TEST_FOLDERS = BASE_ENDPOINT + "tests";
    public static final String TEST(String id) { return TESTS + "/" + id;}

    public static final String CHECKIN_TEST(String id) { return TEST(id) + "/versions/check-in";}
    public static final String CHECKOUT_TEST(String id) { return TEST(id) + "/versions/check-out";}
    
    public static final String TEST_SETS = BASE_ENDPOINT + "test-sets";
    public static final String TEST_SET_FOLDERS = BASE_ENDPOINT + "test-set-folders";
    public static final String TEST_SET(String id) { return TEST_SETS + "/" + id;};
    
    public static final String TEST_INSTANCES = BASE_ENDPOINT + "test-instances";
    public static final String TEST_INSTANCE(String id) { return TEST_INSTANCES + "/" + id;}
    
    public static final String RUNS = BASE_ENDPOINT + "runs";
    public static final String RUN(String id) { return RUNS + "/" + id; }
    
    public static final String RUN_STEPS(String runId) { return RUN(runId + "/run-steps");}  
    public static final String RUN_STEP(String runId, String id) { return RUN_STEPS(runId) + "/" + id;}  
}

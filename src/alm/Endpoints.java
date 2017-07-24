package alm;

/**
 * Created by MARTINCORB on 7/14/2017.
 */
public class Endpoints {
    public static final String AUTHENTICATE = "authentication-point/authenticate";
    public static final String LOGOUT = "authentication-point/logout";
    public static final String ISAUTHENTICATED = "rest/is-authenticated";
    public static final String SITESESSION = "rest/site-session";
    public static final String TESTS = String.format("rest/domains/%s/projects/%s/tests", Config.getDomain(), Config.getProject());
}

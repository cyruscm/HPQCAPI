package alm;

import infrastructure.Constants;

/**
 * Created by MARTINCORB on 7/14/2017.
 */
public class Config {
    private static String Host;
    private static String Port;
    private static String Domain;
    private static String Project;
    private static String Username;
    private static String Password;

    public static String getHost() { return Host; }
    public static String getPort() { return Port; }
    public static String getDomain() { return Domain; }
    public static String getProject() { return Project; }
    public static String getUsername() { return Username; }
    public static String getPassword() { return Password; }

    public static void initConfigs(String[] args) {
        Host = (args[0] != null) ? args[0] : Constants.HOST;
        Port = (args[1] != null) ? args[1] : Constants.PORT;
        Domain = (args[2] != null) ? args[2] : Constants.DOMAIN;
        Project = (args[3] != null) ? args[3] : Constants.PROJECT;
        Username = (args[4] != null) ? args[4] : Constants.USERNAME;
        Password = (args[5] != null) ? args[5] : Constants.PASSWORD;
    }
}

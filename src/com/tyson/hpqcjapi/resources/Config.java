package com.tyson.hpqcjapi.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by MARTINCORB on 7/14/2017.
 */
public class Config {
	private static Properties prop;

	private final static String DEFAULT_CONFIG_PATH = "config.properties";

	public static String getHost() {
		return prop.getProperty("host");
	}

	public static String getPort() {
		return prop.getProperty("port");
	}

	public static String getDomain() {
		return prop.getProperty("domain");
	}

	public static String getProject() {
		return prop.getProperty("project");
	}

	public static String getUsername() {
		return prop.getProperty("username");
	}

	public static String getPassword() {
		return prop.getProperty("password");
	}

	public static String getTeam() {
		return prop.getProperty("team");
	}

	public static String getUnitTestFolderID() {
		return prop.getProperty("testFolder");
	}

	public static String getTestSetName() {
		return prop.getProperty("testSetName");
	}

	public static String getTestSetID() {
		return prop.getProperty("testSetId");
	}

	public static boolean guessTestSet() {
		return (prop.getProperty("guessTestSet") == "true");
	}
	
	public static boolean verbose() {
		return (prop.getProperty("verbose") == "true");
	}

	/**
	 * Prepares the global config options. First reads from config file (or creates if does not
	 * exist). Then overrides configs with input cli args
	 * @param parArgs Map of cli args with name and value
	 * @param parFlags List of cli flags with name
	 * @throws IOException 
	 */
	public static void initConfigs(Map<String, String> parArgs, List<String> parFlags) throws IOException {
		prop = new Properties();
		readConfig((parArgs.get("config") == null) ? DEFAULT_CONFIG_PATH : parArgs.get("config"));
		prop.putAll(parArgs);
		
		for (String flag : parFlags) {
			prop.put(flag, "true");
		}
	}

	/**
	 * Read the config file, create and read if does not exist
	 * @param path
	 * @throws IOException 
	 */
	private static void readConfig(String path) throws IOException {
		try {
			InputStream input = new FileInputStream(path);
			prop.load(input);
		} catch (FileNotFoundException e) {
			createConfig();
		} 
	}

	private static void createConfig() throws IOException {
		prop.setProperty("host", Constants.HOST);
		prop.setProperty("port", Constants.PORT);
		prop.setProperty("domain", Constants.DOMAIN);
		prop.setProperty("project", Constants.PROJECT);
		prop.setProperty("username", Constants.USERNAME);
		prop.setProperty("password", Constants.PASSWORD);
		prop.setProperty("team", Constants.TEAM);
		prop.setProperty("testFolder", Constants.FOLDERID);
		prop.setProperty("testSetId", Constants.TESTSETID);
		prop.setProperty("testSetName", Constants.TESTSETNAME);
		prop.setProperty("guessTestSet", Constants.GUESSTESTSET);

		OutputStream output = new FileOutputStream(DEFAULT_CONFIG_PATH);
		prop.store(output,
				"Default properties to use for HPQCJAPI connections. Please adjust to your needs."
				+ " Note that these can all be ignored with cli args. Also testSetId, testSetName,"
				+ " and guessTestSet are not required. TestSetId links directly to testset, name"
				+ " attempts to find the id matching the exact provided name, guess will simply"
				+ " use the tesetset folder from the last run (please mark guessTestSet with either true or false).");
	}
}

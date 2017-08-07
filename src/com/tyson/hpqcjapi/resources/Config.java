package com.tyson.hpqcjapi.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import com.hpe.infrastructure.Constants;

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
		return prop.getProperty("testfolder");
	}

	public static String getTestSetName() {
		return prop.getProperty("testsetname");
	}

	public static String getTestSetID() {
		return prop.getProperty("testsetid");
	}

	public static boolean guessTestSet() {
		return ((prop.getProperty("guesstestset") != null) ? 
				(prop.getProperty("guesstestset").equals("false") ? false : true) 
				: true);
	}

	public static void initConfigs(Map<String, String> parArgs) {
		prop = new Properties();
		readConfig((parArgs.get("config") == null) ? DEFAULT_CONFIG_PATH : parArgs.get("config"));
		prop.putAll(parArgs);
	}

	private static void readConfig(String path) {
		try {
			InputStream input = new FileInputStream(path);
			prop.load(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void createConfig() {
		prop.setProperty("host", Constants.HOST);
		prop.setProperty("port", Constants.PORT);
		prop.setProperty("domain", Constants.DOMAIN);
		prop.setProperty("project", Constants.PROJECT);
		prop.setProperty("username", Constants.USERNAME);
		prop.setProperty("password", Constants.PASSWORD);
		prop.setProperty("team", Constants.TEAM);
		prop.setProperty("testfolder", Constants.FOLDERID);
		prop.setProperty("testsetid", Constants.TESTSETID);
		prop.setProperty("testsetname", Constants.TESTSETNAME);
		prop.setProperty("guesstestset", Constants.GUESSTESTSET);

		try {
			OutputStream output = new FileOutputStream(DEFAULT_CONFIG_PATH);
			prop.store(output,
					"Default properties to use for HPQCJAPI connections. Please adjust to your needs."
					+ " Note that these can all be ignored with cli args. Also testsetid, testsetname,"
					+ " and guesstestset are not required. TestSetId links directly to testset, name"
					+ " attempts to find the id matching the exact provided name, guess will simply"
					+ " use the tesetset folder from the last run (please mark guesstestset with either true or false).");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

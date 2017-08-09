package com.tyson.hpqcjapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MARTINCORB on 7/14/2017.
 */

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.hpe.infrastructure.Base64Encoder;
import com.tyson.hpqcjapi.exceptions.HPALMRestException;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.resources.Constants;
import com.tyson.hpqcjapi.utils.Logger;

/**
 * Allows upload and synchronization of JUnit output xml into HPQC.
 * If you're reading this after we've upgraded to 12.5, then don't bother. This is deprecated.
 * Please see http://alm-help.saas.hpe.com/en/12.53/online_help/Content/UG/t_integrate_external_tests_to_alm.htm
 * @author MARTINCORB
 *
 */
public class HPQCJAPI {
	
	public static boolean valid_input = true;
	
	public static void main(String[] args) throws Exception {
		try {
			if (prepareSteps(args)) {
				run(Config.getJunitPath());
			} else {
				Logger.logDebug("Failed input validation");
			}
		} catch (HPALMRestException e) {
			Logger.logDebug("Response Failure: " + e.getResponse().getFailure());
			Logger.logDebug("Response Status: " + e.getResponse().getStatusCode());
			Logger.logDebug("Response Headers: " + e.getResponse().getResponseHeaders());
			Logger.logDebug("Response Body: " + new String(e.getResponse().getResponseData()));
			throw(e);
		}
	}
	
	/**
	 * Prepares all needed configs and steps.
	 * @return True if ready, false if not.
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static boolean prepareSteps(String[] args) throws ParseException, IOException {
		// Property Explanation: https://stackoverflow.com/questions/45152242/hp-alm-rest-api-qcsession-411-authentication/45153033#45153033
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
	
		List<String> finalArgs = initConfig(args);
		if (!valid_input) { 
			return false; 
		}
		
		if (finalArgs.size() > 1) {
			Logger.logError("There are too many arguments. Expecting only JUnit Output path");
			valid_input = false;
			return false;
		} else if (finalArgs.size() < 1) {
			Logger.logError("There are not enough arguments. Expecting JUnit output path");
			valid_input = false;
			return false;
		} else {
			Config.setJunitPath(finalArgs.get(0));
			return true;
		}
	}
	
	
	/**
	 * Prepares the config file from the inputed arguments
	 * @param args The list of inputed args
	 * @return The remaining arguments after parsing with CommonsCLI
	 * @throws ParseException
	 * @throws IOException
	 */
	private static List<String> initConfig(String[] args) throws ParseException, IOException {
		Options options = initOptions();
		CommandLineParser parser = new DefaultParser();
		
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			Logger.logError(e.getLocalizedMessage());
			Logger.logError("Use option -h for help on arguments");
			valid_input = false;
			return new ArrayList<String>();
		}
		
		
		if (commandLine.hasOption("h")) {
			printHelp(options);
		}
		
		Map<String, String> parArgs = new HashMap<String, String>();
		List<String> parFlags = new ArrayList<String>();
		for (Option o : commandLine.getOptions()) {
			if (o.hasArg()) {
				parArgs.put(o.getLongOpt(), o.getValue());
			} else {
				parFlags.add(o.getLongOpt());
			}
		}

		Config.initConfigs(parArgs, parFlags);
		
		
		if (Config.getTestId() == null || Config.getTestId().isEmpty()) {
			if (Config.getTestName() == null || Config.getTestName().isEmpty()) {
				Logger.logDebug("id:" + Config.getTestId() + " -- Name: " + Config.getTestName());
				Logger.logError("You must either give a test id (--testId [id]) or set a test name (--testName [name])");
				valid_input = false;
			}
		}
		
		
		return commandLine.getArgList();
	}
	
	/**
	 * Create all options
	 * @return
	 */
	private static Options initOptions() {
		Options options = new Options();
		
		options.addOption(addOpt("host", "H", "The host domain to connect to. (Defaults to " + Constants.HOST + ")"));
		options.addOption(addOpt("port", "P", "The port to connect to (defaults " + Constants.PORT + ")"));
		options.addOption(addOpt("domain", "d", "The domain in ALM to connect to. (defaults to " + Constants.DOMAIN + ")"));
		options.addOption(addOpt("project", "D", "The project name to connect to (defaults to " + Constants.PROJECT + ")"));
		options.addOption(addOpt("username", "u", "The username to authenticate with. (defaults to empty)"));
		options.addOption(addOpt("password", "p", "The password to authenticate with. (defaults to empty)"));
		options.addOption(addOpt("team", "T", "The process team to set for the test (defaults to "+ Constants.TEAM + ")"));
		options.addOption(addOpt("testFolder", "f", "The test set folder id to use (defaults to "+ Constants.FOLDERID + ", Unit Testing)"));
		options.addOption(addOpt("testSetId", "s", "Test set id to use [takes precedent over testSetName and guessTestSet] (defaults to empty)"));
		options.addOption(addOpt("testSetName", "S", "Test set name to use [takes precedent over guessTestSet] (defaults to empty)"));
		options.addOption(addFlag("createTest", "c", "Create a test if one is not found", false));
		options.addOption(addOpt("testId", "t", "The ID of the test to use. Defaults to empty"));
		options.addOption(addOpt("testName", "n", "The name of the test to use. Defaults to empty. REQUIRED if not setting testId"));
		options.addOption(addFlag("help", "h", "Print help information", false));
		options.addOption(addFlag("guessTestSet", "g", "Flag: Attempt to guess the test set. Currently does so by using the test set from the last executed run in HPQC", false));
		options.addOption(addFlag("verbose", "v", "Flag: Output debug information", false));
		
		return options;
	}
	
	/**
	 * Print help message and shut down.
	 * @param options
	 */
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		String header = Constants.NAME + " [...] [JUNITPATH] [TEST_NAME]";
		String footer = "Created 8/8/2017. Property of Tyson Foods Inc.";
		formatter.setWidth(100);
		formatter.printHelp(Constants.NAME + " " + Constants.VERSION, header, options, footer, false);
		System.exit(0);
	}
	
	private static Option addOpt(String longName, String shortName, String message) {
		return addOpt(longName, shortName, message, false);
	}
	
	private static Option addOpt(String longName, String shortName, String message, boolean required) {
		return Option.builder(shortName).required(required)
				.desc(message).longOpt(longName).hasArg().build();
	}
	
	private static Option addFlag(String longName, String shortName, String message, boolean required) {
		return Option.builder(shortName).required(required)
				.desc(message).longOpt(longName).build();
	}

	
	/**
	 * Do all the steps n stuff
	 * @param name Name of test, casing very important. This is the primary key
	 * @param path Path to JUnit output file.
	 * @throws Exception 
	 */
	public static boolean run(String path) throws Exception {
		
		JUnitReader reader = new JUnitReader(path);
		
		ByteArrayOutputStream bos = null;
		try {
		    bos = new ByteArrayOutputStream();
		    ObjectOutputStream obj_out = new ObjectOutputStream(bos);
		    obj_out.writeObject(reader.parseSuites());
		} catch (IOException e) {
		    e.printStackTrace();
		}

		byte[] serialized = bos.toByteArray();
		String encoded = Base64Encoder.encode(serialized);
		
		System.out.print("The serialized output is: " + encoded); 
		
		/**
		JUnitPoster poster = new JUnitPoster(reader);
		
		Logger.log("Beginning publishing JUnit results to HPQC...");
		String testId = poster.getTestId();
		Logger.logDebug("Found testID: " + testId);
		
		Logger.log("Synching testcases with HPQC...");
		poster.syncTestSteps(testId);
		
		Logger.log("Determining which test set to use...");
		String testSetId = poster.getTestSetId();
		Logger.logDebug("Using Test Set " + testSetId);
		
		Logger.log("Determining test instance details...");
		String testInstanceId = poster.getTestInstanceId(testId, testSetId);
		Logger.logDebug("Found testInstanceID: " + testInstanceId);
		
		Logger.log("Preparing a run for test instance...");
		Date date = new Date();
		String formattedDate = new SimpleDateFormat("M-d_H-m-s").format(date);
		String runName = "Run_" + formattedDate;
		String runId = poster.createRun(runName, testId, testSetId, testInstanceId);
		Logger.logDebug("Created run with runID:" + runId);
		
		Logger.log("Synchronizing testcase results with HPQC run");
		poster.syncRunSteps(runId);
		Logger.log("Finished."); **/
		return true;
	}
	
}
package com.tyson.hpqcjapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

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
import com.hpe.infrastructure.Constants;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.utils.Logger;

/**
 * Allows upload and synchronization of JUnit ouput xml into HPQC.
 * If you're reading this after we've upgraded to 12.5, then don't bother. This is deprecated.
 * Please see http://alm-help.saas.hpe.com/en/12.53/online_help/Content/UG/t_integrate_external_tests_to_alm.htm
 * @author MARTINCORB
 *
 */
public class HPQCJAPI {
	public static void main(String[] args) throws Exception {
		// https://stackoverflow.com/questions/45152242/hp-alm-rest-api-qcsession-411-authentication/45153033#45153033
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		
		List<String> finalArgs = initConfig(args);
		
		if (finalArgs.size() > 2) {
			Logger.logError("There are too many arguments. Expecting only JUnit Output path and the project name");
		} else if (finalArgs.size() < 2) {
			Logger.logError("There are not enough arguments. Expecting JUnit output path and project name");
		} else {
			//run(finalArgs.get(0), finalArgs.get(1));
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
		
		
		CommandLine commandLine = parser.parse(options, args);
		
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
		options.addOption(addOpt("team", "t", "The process team to set for the test (defaults to "+ Constants.TEAM + ")"));
		options.addOption(addOpt("testsetfolder", "f", "The test set folder id to use (defaults to "+ Constants.FOLDERID + ", Unit Testing)"));
		options.addOption(addOpt("testSetId", "s", "Test set id to use [takes precedent over testSetName and guessTestSet] (defaults to empty)"));
		options.addOption(addOpt("testSetName", "S", "Test set name to use [takes precedent over guessTestSet] (defaults to empty)"));
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
		String header = "[...] [JUNITPATH] [TEST_NAME]";
		String footer = "Created 8/8/2017. Property of Tyson Foods Inc.";
		formatter.setWidth(100);
		formatter.printHelp("HPQCJAPI", header, options, footer, false);
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
	public static void run(String name, String path) throws Exception {
		JUnitReader reader = new JUnitReader(path);
		JUnitPoster poster = new JUnitPoster(name, reader);
		Logger.logDebug("Inputted Tests: " + poster.toString());
		
		Logger.log("Beginning publishing JUnit results to HPQC...");
		String testId = poster.getTestId(name);
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
		String runId = poster.createRun(name, testId, testSetId, testInstanceId);
		Logger.logDebug("Created run with runID:" + runId);
		
		Logger.log("Synchronizing testcase results with HPQC run");
		poster.syncRunSteps(runId);
		
		Logger.logDebug("Linked Tests: " + poster.toString());
	}
	
}
package com.tyson.hpqcjapi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by MARTINCORB on 7/14/2017.
 */

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.hpe.infrastructure.Constants;
import com.tyson.hpqcjapi.resources.Config;
import com.tyson.hpqcjapi.types.LinkedTestCase;
import com.tyson.hpqcjapi.utils.Logger;

public class HPQCJAPI {
	public static void main(String[] args) {
		// https://stackoverflow.com/questions/45152242/hp-alm-rest-api-qcsession-411-authentication/45153033#45153033
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		CommandLine commandLine;
		Options options = new Options();
		
		options.addOption(addOpt("host", "n", "The host domain to connect to. (Defaults to " + Constants.HOST + ")"));
		options.addOption(addOpt("port", "po", "The port to connect to (defaults " + Constants.PORT + ")"));
		options.addOption(addOpt("domain", "d", "The domain in ALM to connect to. (defaults to " + Constants.DOMAIN + ")"));
		options.addOption(addOpt("project", "pr", "The project name to connect to (defaults to " + Constants.PROJECT + ")"));
		options.addOption(addOpt("username", "u", "The username to authenticate with. (defaults to " + Constants.USERNAME + ")"));
		options.addOption(addOpt("password", "p", "The password to authenticate with. (defaults to password for "+ Constants.USERNAME + ")"));
		
		CommandLineParser parser = new DefaultParser();

		String[] parameters = new String[6];
		try {
			commandLine = parser.parse(options, args);
			Map<String, String> parArgs = new HashMap<String, String>();
			Consumer<Option> cons = (Consumer<Option>) o -> parArgs.put(o.getLongOpt(), o.getValue());
			commandLine.iterator().forEachRemaining(cons);

			Config.initConfigs(parArgs);
			//run();

		} catch (ParseException exception) {
			System.out.print("Parse error: ");
			System.out.println(exception.getMessage());
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	private static Option addOpt(String longName, String shortName, String message) {
		return addOpt(longName, shortName, message, false);
	}
	
	private static Option addOpt(String longName, String shortName, String message, boolean required) {
		return Option.builder(shortName).required(required)
				.desc(message).longOpt(longName).hasArg().build()
	}

	public static void run() {
		String name = "HPQCJAPI-Diff-Test";
		String path = "C:/Users/MARTINCORB/eclipse-workspace/HPQCJAPI/sampleJunit.xml";
		JUnitReader reader = new JUnitReader(path);
		JUnitPoster poster = new JUnitPoster(name, reader);
		Logger.logDebug(poster.toString());
		poster.publishTest();
		Logger.logDebug(poster.toString());

		Logger.logDebug("fin");

	}
}
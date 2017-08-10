package com.tyson.hpqcjapi.utils;

import com.tyson.hpqcjapi.resources.Config;

public class Logger {

	private static final String LOGPREFIX = "[INFO] ";
	private static final String WARNPREFIX = "[WARN] ";
	private static final String DEBUGPREFIX = "[DEBUG] ";
	private static final String ERRORPREFIX = "[ERROR] ";

	public static void log(String input) {
		System.out.println(LOGPREFIX + input);
	}

	public static void logWarning(String input) {
		System.out.println(WARNPREFIX + input);
	}

	public static void logError(String input) {
		System.out.println(ERRORPREFIX + input);
	}

	public static void logDebug(String input) {
		if (Config.verbose()) {
			System.out.println(DEBUGPREFIX + input);
		}
	}
}

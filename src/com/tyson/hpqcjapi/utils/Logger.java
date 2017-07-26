package com.tyson.hpqcjapi.utils;

public class Logger {

    private static final String LOGPREFIX  = "";
    private static final String WARNPREFIX = "[WARN] ";
    private static final String DEBUGPREFIX= "[DEBUG] ";
    private static final String ERRORPREFIX= "[ERROR] ";

    public static boolean debug = false;

    public static void log(String input) {
        System.out.println(LOGPREFIX + input);
    }


    public static void logWarning(String input) {
        System.out.println(WARNPREFIX + input);
    }


    public static void logError(String input) {
        if (debug) {
            System.out.println(ERRORPREFIX + input);
        }
    }


    public static void logDebug(String input) {
        System.out.println(DEBUGPREFIX + input);
    }
}

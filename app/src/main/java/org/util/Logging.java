package org.util;

public class Logging {
    public static void logError(String message) {
        System.out.println("ERROR: \u001B[31m" + message + "\u001B[0m");
    }

    public static void logError(Exception e) {
        System.out.println("ERROR: \u001B[31m" + e.getMessage() + "\u001B[0m");
    }

    public static void logInfo(String message) {
        System.out.println("INFO: " + message);
    }

    public static void logWarning(String message) {
        System.out.println("WARNING: \u001B[33m" + message + "\u001B[0m");
    }

    public static void logSuccess(String message) {
        System.out.println("SUCCESS: \u001B[32m" + message + "\u001B[0m");
    }
}

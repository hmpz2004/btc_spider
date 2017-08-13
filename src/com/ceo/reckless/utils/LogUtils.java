package com.ceo.reckless.utils;

public class LogUtils {

    public static void logDebugLine(String s) {
        System.out.println(s);
    }

    public static void logDebug(String s) {
        System.out.print(s);
    }

    public static void logError(Exception e) {
        e.printStackTrace();
    }
}

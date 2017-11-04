package com.ceo.reckless.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtils {

    private static final int THREAD_STR_LENGTH = 20;
    private static final int STACK_STR_LENGTH = 30;

    private static final String LOG_FORMATTER = "❖ %s %s ❖   %s";

    private static SimpleDateFormat sFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static void logDebugLine(String s) {
//        System.out.println(s);

        System.out.println(String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), s));
    }

    public static void logDebug(String s) {
//        System.out.print(s);
        System.out.print(String.format(LOG_FORMATTER, threadName(), stackInfo(new Throwable().getStackTrace()), s));
    }

    public static void logError(Exception e) {
        e.printStackTrace();
    }


    /**
     * 调用栈信息
     * 只打印调用log的上一个栈的信息
     */
    private static String stackInfo(StackTraceElement[] traces) {
        String str = "";
        if (traces.length > 1 && traces[1] != null) {
            String fileName = traces[1].getFileName();
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                fileName = fileName.substring(0, index);
            }
            str = String.format("%s.%d.%s()", fileName, traces[1].getLineNumber(), traces[1].getMethodName());
        }
        return fixStringLength(str, STACK_STR_LENGTH);
    }

    /**
     * 线程信息
     */
    private static String threadName() {
        return fixStringLength(Thread.currentThread().getName()
                + " " + time(), THREAD_STR_LENGTH);
    }

    /**
     * 输出固定长度的字符串
     * <p/>
     * 不够被空格, 过长做trim()
     */
    private static String fixStringLength(String s, int targetLen) {
        if (s != null && targetLen > 0) {
            int len = s.length();
            if (len > targetLen) {
                return s.substring(0, targetLen);
            }

            StringBuilder sb = new StringBuilder(s);
            while (len < targetLen) {
                sb.append(" ");
                len++;
            }
            return sb.toString();
        }
        return "";
    }
    private static String time() {
        return sFormatter.format(new Date());
    }
}

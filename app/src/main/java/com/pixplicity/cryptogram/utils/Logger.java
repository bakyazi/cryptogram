package com.pixplicity.cryptogram.utils;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {

    private static final int CALL_STACK_INDEX = 2;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");

    private static final int VERBOSE = 0;
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int WARN = 3;
    private static final int ERROR = 4;

    @SuppressWarnings("unused")
    public static void v(String tag, String message) {
        log(VERBOSE, tag, message, null);
    }

    @SuppressWarnings("unused")
    public static void v(String tag, String message, Throwable throwable) {
        log(VERBOSE, tag, message, throwable);
    }

    @SuppressWarnings("unused")
    public static void d(String tag, String message) {
        log(DEBUG, tag, message, null);
    }

    @SuppressWarnings("unused")
    public static void d(String tag, String message, Throwable throwable) {
        log(DEBUG, tag, message, throwable);
    }

    @SuppressWarnings("unused")
    public static void i(String tag, String message) {
        log(INFO, tag, message, null);
    }

    @SuppressWarnings("unused")
    public static void i(String tag, String message, Throwable throwable) {
        log(INFO, tag, message, throwable);
    }

    @SuppressWarnings("unused")
    public static void w(String tag, String message) {
        log(WARN, tag, message, null);
    }

    @SuppressWarnings("unused")
    public static void w(String tag, String message, Throwable throwable) {
        log(WARN, tag, message, throwable);
    }

    @SuppressWarnings("unused")
    public static void e(String tag, String message) {
        log(ERROR, tag, message, null);
    }

    @SuppressWarnings("unused")
    public static void e(String tag, String message, Throwable throwable) {
        log(ERROR, tag, message, throwable);
    }

    private static void log(int priority, String tag, String message, Throwable throwable) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length <= CALL_STACK_INDEX) {
            throw new IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?");
        }
        String clazz = extractClassName(stackTrace[CALL_STACK_INDEX]);
        int lineNumber = stackTrace[CALL_STACK_INDEX].getLineNumber();
        message = ".(" + clazz + ".java:" + lineNumber + ") - " + message;
        switch (priority) {
            case VERBOSE:
                Log.v(tag, message, throwable);
                break;
            case DEBUG:
                Log.d(tag, message, throwable);
                break;
            case INFO:
                Log.i(tag, message, throwable);
                break;
            case WARN:
                Log.w(tag, message, throwable);
                break;
            case ERROR:
                Log.e(tag, message, throwable);
                break;
        }
    }

    /**
     * Extract the class name without any anonymous class suffixes (e.g., {@code Foo$1}
     * becomes {@code Foo}).
     */
    private static String extractClassName(StackTraceElement element) {
        String tag = element.getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        return tag.substring(tag.lastIndexOf('.') + 1);
    }

}

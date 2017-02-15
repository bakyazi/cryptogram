package com.pixplicity.cryptogram.utils;

import java.util.Locale;

public class StringUtils {

    private final static String NON_THIN = "[^iIl1\\.,']";

    private static int textWidth(String str) {
        return str.length() - str.replaceAll(NON_THIN, "").length() / 2;
    }

    public static String ellipsize(String text, int max) {
        if (textWidth(text) <= max) {
            return text;
        }

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters…
        int end = text.lastIndexOf(' ', max - 1);

        // Just one long word. Chop it off.
        if (end == -1) {
            return text.substring(0, max - 1) + "…";
        }

        // Step forward as long as textWidth allows.
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = text.indexOf(' ', end + 1);

            // No more spaces.
            if (newEnd == -1) {
                newEnd = text.length();
            }

        } while (textWidth(text.substring(0, newEnd) + "…") < max);

        return text.substring(0, end) + "…";
    }
    
    public static String getDurationString(long durationMs) {
        int durationS = (int) (durationMs / 1000);
        return String.format(
                Locale.ENGLISH,
                "%d:%02d:%02d",
                durationS / 3600,
                durationS % 3600 / 60,
                durationS % 60);
    }

}

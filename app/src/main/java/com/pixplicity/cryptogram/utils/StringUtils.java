package com.pixplicity.cryptogram.utils;

import java.util.Locale;

public class StringUtils {

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

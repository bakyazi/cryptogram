package com.pixplicity.cryptogram.utils;

import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.HashSet;
import java.util.Set;

public class PrefsUtils {

    private static final String KEY_CURRENT_ID = "current_puzzle_index";
    private static final String KEY_PROGRESS = "puzzle_progress";
    private static final String KEY_RANDOMIZE = "randomize";
    private static final String KEY_ONBOARDING = "onboarding";
    private static final String KEY_SHOW_HINTS = "show_hints";
    private static final String KEY_DARK_THEME = "dark_theme";

    public static int getCurrentId() {
        return Prefs.getInt(PrefsUtils.KEY_CURRENT_ID, -2);
    }

    public static void setCurrentId(int currentId) {
        Prefs.putInt(PrefsUtils.KEY_CURRENT_ID, currentId);
    }

    public static Set<String> getProgress() {
        if (CryptogramApp.getInstance() == null) {
            return null;
        }
        return Prefs.getOrderedStringSet(PrefsUtils.KEY_PROGRESS, null);
    }

    public static void setProgress(Set<String> progressStrSet) {
        if (CryptogramApp.getInstance() == null) {
            return;
        }
        Prefs.putOrderedStringSet(PrefsUtils.KEY_PROGRESS, progressStrSet);
    }

    public static boolean getRandomize() {
        return Prefs.getBoolean(KEY_RANDOMIZE, false);
    }

    public static void setRandomize(boolean randomize) {
        Prefs.putBoolean(KEY_RANDOMIZE, randomize);
    }

    public static void setOnboarding(int page) {
        Prefs.putInt(KEY_ONBOARDING, page);
    }

    public static int getOnboarding() {
        return Prefs.getInt(KEY_ONBOARDING, -1);
    }

    public static boolean getShowHints() {
        return Prefs.getBoolean(KEY_SHOW_HINTS, false);
    }

    public static void setShowHints(boolean show) {
        Prefs.putBoolean(KEY_SHOW_HINTS, show);
    }

    public static void setDarkTheme(boolean theme) {
        Prefs.putBoolean(KEY_DARK_THEME, theme);
    }

    public static boolean getDarkTheme() {
        return Prefs.getBoolean(KEY_DARK_THEME, false);
    }

}

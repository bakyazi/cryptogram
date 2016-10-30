package com.pixplicity.cryptogram.utils;

import android.util.SparseArray;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.Set;

public class PrefsUtils {

    public static final String KEY_CURRENT_ID = "current_puzzle_index";
    public static final String KEY_PROGRESS = "puzzle_progress";

    private static SparseArray<String> sPuzzleProgress;

    public static int getCurrentId() {
        return Prefs.getInt(PrefsUtils.KEY_CURRENT_ID, -1);
    }

    public static void setCurrentId(int currentId) {
        Prefs.putInt(PrefsUtils.KEY_CURRENT_ID, currentId);
    }

    public static Set<String> getProgress() {
        return Prefs.getOrderedStringSet(PrefsUtils.KEY_PROGRESS, null);
    }

    public static void setProgress(Set<String> progressStrSet) {
        Prefs.putOrderedStringSet(PrefsUtils.KEY_PROGRESS, progressStrSet);
    }

}

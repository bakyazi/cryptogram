package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.pixplicity.easyprefs.library.Prefs;

public class UpdateManager {

    private static boolean sEnabledShowUsedLetters;
    private static boolean sScoreExcludesExcessInputs;

    public static void init(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int lastVersion = PrefsUtils.getLastVersion();
            if (lastVersion < info.versionCode) {
                // App has just been updated
                PrefsUtils.setLastVersion(info.versionCode);
                if (lastVersion >= 0) {
                    // Only if this isn't a fresh install
                    if (lastVersion < 189) {
                        sEnabledShowUsedLetters = true;
                        // Remove old setting
                        Prefs.remove("show_hints");
                    }
                    if (lastVersion < 190) {
                        sScoreExcludesExcessInputs = true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    public static boolean consumeEnabledShowUsedLetters() {
        if (sEnabledShowUsedLetters) {
            sEnabledShowUsedLetters = false;
            return true;
        }
        return false;
    }

    public static boolean consumeScoreExcludesExcessInputs() {
        if (sScoreExcludesExcessInputs) {
            sScoreExcludesExcessInputs = false;
            return true;
        }
        return false;
    }

}

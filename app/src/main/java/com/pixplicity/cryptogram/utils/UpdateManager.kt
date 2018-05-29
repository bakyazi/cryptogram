package com.pixplicity.cryptogram.utils

import android.content.Context
import android.content.pm.PackageManager
import com.pixplicity.easyprefs.library.Prefs

object UpdateManager {

    private var sEnabledShowUsedLetters: Boolean = false
    private var sScoreExcludesExcessInputs: Boolean = false

    fun init(context: Context) {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val lastVersion = PrefsUtils.lastVersion
            if (lastVersion < info.versionCode) {
                // App has just been updated
                PrefsUtils.lastVersion = info.versionCode
                if (lastVersion >= 0) {
                    // Only if this isn't a fresh install
                    if (lastVersion < 189) {
                        sEnabledShowUsedLetters = true
                        // Remove old setting
                        Prefs.remove("show_hints")
                    }
                    if (lastVersion < 190) {
                        sScoreExcludesExcessInputs = true
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }

    }

    fun consumeEnabledShowUsedLetters(): Boolean {
        if (sEnabledShowUsedLetters) {
            sEnabledShowUsedLetters = false
            return true
        }
        return false
    }

    fun consumeScoreExcludesExcessInputs(): Boolean {
        if (sScoreExcludesExcessInputs) {
            sScoreExcludesExcessInputs = false
            return true
        }
        return false
    }

}

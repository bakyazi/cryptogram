package com.pixplicity.cryptogram

import android.app.Application
import android.content.ContextWrapper

import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.pixplicity.cryptogram.utils.UpdateManager
import com.pixplicity.easyprefs.library.Prefs

import io.fabric.sdk.android.Fabric


class CryptogramApp : Application() {

    lateinit var firebaseAnalytics: FirebaseAnalytics
        private set

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Initialize Crashlytics
        Fabric.with(this, Crashlytics())

        // Initialize the Prefs class
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()

        UpdateManager.init(this)
    }

    companion object {

        val CONTENT_ACHIEVEMENTS = "achievements"
        val CONTENT_LEADERBOARDS = "leaderboards"
        val CONTENT_STATISTICS = "statistics"
        val CONTENT_SETTINGS = "settings"
        val CONTENT_HOW_TO_PLAY = "how-to-play"
        val CONTENT_DONATE_SUGGESTION = "donate_suggestion"
        val CONTENT_DONATE = "donate"
        val CONTENT_ABOUT = "about"
        val EVENT_LEVEL_START = "level_start"
        val EVENT_LEVEL_END = "level_end"

        var instance: CryptogramApp? = null

    }

}

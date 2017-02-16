package com.pixplicity.cryptogram;

import android.app.Application;
import android.content.ContextWrapper;

import com.crashlytics.android.Crashlytics;
import com.pixplicity.easyprefs.library.Prefs;

import io.fabric.sdk.android.Fabric;


public class CryptogramApp extends Application {

    public static final String CONTENT_ACHIEVEMENTS = "achievements";
    public static final String CONTENT_LEADERBOARDS = "leaderboards";
    public static final String CONTENT_STATISTICS = "statistics";
    public static final String CONTENT_ABOUT = "about";

    private static CryptogramApp sInstance;

    public CryptogramApp() {
        super();
        sInstance = this;
    }

    public static CryptogramApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crashlytics
        Fabric.with(this, new Crashlytics());

        // Initialize the Prefs class
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

}

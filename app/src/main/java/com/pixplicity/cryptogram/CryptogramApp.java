package com.pixplicity.cryptogram;

import android.app.Application;
import android.content.ContextWrapper;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.cryptogram.utils.UpdateManager;
import com.pixplicity.easyprefs.library.Prefs;

import io.fabric.sdk.android.Fabric;


public class CryptogramApp extends Application {

    public static final String CONTENT_ACHIEVEMENTS = "achievements";
    public static final String CONTENT_LEADERBOARDS = "leaderboards";
    public static final String CONTENT_STATISTICS = "statistics";
    public static final String CONTENT_SETTINGS = "settings";
    public static final String CONTENT_HOW_TO_PLAY = "how-to-play";
    public static final String CONTENT_ABOUT = "about";

    private static CryptogramApp sInstance;

    private FirebaseAnalytics mFirebaseAnalytics;

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

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Crashlytics
        Fabric.with(this, new Crashlytics());

        // Initialize the Prefs class
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        UpdateManager.init(this);
    }

}

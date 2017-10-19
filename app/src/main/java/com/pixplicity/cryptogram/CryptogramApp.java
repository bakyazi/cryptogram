package com.pixplicity.cryptogram;

import android.app.Application;
import android.content.ContextWrapper;

import com.crashlytics.android.Crashlytics;
import com.pixplicity.easyprefs.library.Prefs;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;


public class CryptogramApp extends Application {

    private static final String TAG = CryptogramApp.class.getSimpleName();

    public static final String CONTENT_LANDING = "landing";
    public static final String CONTENT_ACHIEVEMENTS = "achievements";
    public static final String CONTENT_LEADERBOARDS = "leaderboards";
    public static final String CONTENT_STATISTICS = "statistics";
    public static final String CONTENT_SETTINGS = "settings";
    public static final String CONTENT_CONTRIBUTE = "contribute";
    public static final String CONTENT_HOW_TO_PLAY = "how-to-play";
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

        // Prepare Realm
        Realm.init(this);
    }

}

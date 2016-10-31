package com.pixplicity.cryptogram;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.pixplicity.easyprefs.library.Prefs;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class CryptogramApp extends Application {

    private static Context sInstance;

    public CryptogramApp() {
        super();
        sInstance = this;
    }

    public static Context getInstance() {
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

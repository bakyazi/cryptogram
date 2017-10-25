package com.pixplicity.cryptogram;

import android.app.Application;
import android.content.ContextWrapper;

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.pixplicity.cryptogram.services.CryptogramJobService;
import com.pixplicity.cryptogram.utils.UpdateManager;
import com.pixplicity.easyprefs.library.Prefs;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;


public class CryptogramApp extends Application {

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

        UpdateManager.init(this);

        // Prepare Realm
        Realm.init(this);

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        int windowStart = 12 * 60 * 60;
        int windowEnd = (int) (windowStart * 1.5);
        Job periodicDownloadJob = dispatcher.newJobBuilder()
                                            .setService(CryptogramJobService.class)
                                            .setTag(CryptogramJobService.TAG_PERIODIC_DOWNLOAD)
                                            .setConstraints(
                                                    // only run on an unmetered network
                                                    Constraint.ON_UNMETERED_NETWORK,
                                                    // only run when the device is charging
                                                    Constraint.DEVICE_CHARGING
                                            )
                                            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                                            .setTrigger(Trigger.executionWindow(windowStart, windowEnd))
                                            .setRecurring(true)
                                            .build();
        dispatcher.mustSchedule(periodicDownloadJob);
    }

}

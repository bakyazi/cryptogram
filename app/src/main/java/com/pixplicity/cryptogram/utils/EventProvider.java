package com.pixplicity.cryptogram.utils;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class EventProvider {

    private static EventProvider sInstance;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Bus mBus = new Bus();

    public static EventProvider getInstance() {
        if (sInstance == null) {
            sInstance = new EventProvider();
        }
        return sInstance;
    }

    private EventProvider() {
    }

    public static Bus getBus() {
        return getInstance().mBus;
    }

    public static void postEvent(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            getBus().post(event);
        } else {
            postEventDelayed(event);
        }
    }

    public static boolean postEventDelayed(final Object event) {
        return getInstance().mHandler.post(() -> getBus().post(event));
    }

}

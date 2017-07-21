package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.content.IntentSender;

public interface GoogleApiClientResultInterface {

    boolean hasResolution();

    void startResolutionForResult(Context context, int requestCode) throws IntentSender.SendIntentException;

}

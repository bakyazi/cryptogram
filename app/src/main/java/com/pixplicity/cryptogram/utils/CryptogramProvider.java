package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class CryptogramProvider {

    private static final String TAG = CryptogramProvider.class.getSimpleName();

    private static final String KEY_CURRENT_ID = "current_puzzle_index";

    private static CryptogramProvider sInstance;

    private int mCurrentId = -1;
    private Cryptogram[] mCryptograms;

    private Gson mGson = new Gson();

    @NonNull
    public static CryptogramProvider getInstance(Context context) {
        if (sInstance == null) {
            try {
                sInstance = new CryptogramProvider(context);
            } catch (IOException e) {
                Log.e(TAG, "could not read cryptogram file", e);
                Toast.makeText(context, "Could not find any cryptograms", Toast.LENGTH_LONG).show();
            }
        }
        return sInstance;
    }

    private CryptogramProvider(Context context) throws IOException {
        InputStream is = context.getAssets().open("cryptograms.json");
        mCryptograms = mGson.fromJson(new InputStreamReader(is), Cryptogram[].class);
    }

    private Cryptogram[] getAll() {
        return mCryptograms;
    }

    public int getCount() {
        return getAll().length;
    }

    @Nullable
    public Cryptogram getCurrent() {
        if (mCurrentId < 0) {
            mCurrentId = Prefs.getInt(KEY_CURRENT_ID, -1);
        }
        if (mCurrentId < 0) {
            int count = getCount();
            if (count == 0) {
                return null;
            }
            mCurrentId = new Random().nextInt(count);
            Prefs.putInt(KEY_CURRENT_ID, mCurrentId);
        }
        return mCryptograms[mCurrentId];
    }

}

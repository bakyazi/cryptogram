package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.models.CryptogramProgress;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class CryptogramProvider {

    private static final String TAG = CryptogramProvider.class.getSimpleName();

    private static CryptogramProvider sInstance;

    private int mCurrentId = -1;
    private Cryptogram[] mCryptograms;
    private SparseArray<CryptogramProgress> mCryptogramProgress;

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
        int i = 1000000;
        for (Cryptogram cryptogram : mCryptograms) {
            if (cryptogram.getId() == 0) {
                cryptogram.setId(i);
                i++;
            }
        }
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
            mCurrentId = PrefsUtils.getCurrentId();
        }
        if (mCurrentId < 0) {
            int count = getCount();
            if (count == 0) {
                return null;
            }
            mCurrentId = new Random().nextInt(count);
            PrefsUtils.setCurrentId(mCurrentId);
        }
        return mCryptograms[mCurrentId];
    }

    @Nullable
    public Cryptogram getNext() {
        int oldId = mCurrentId;
        mCurrentId = -1;
        Cryptogram cryptogram = getCurrent();
        if (mCurrentId == oldId) {
            // If the puzzle didn't change, simply take the next puzzle
            if (oldId + 1 < getCount()) {
                mCurrentId = oldId + 1;
            } else {
                mCurrentId = 0;
            }
            PrefsUtils.setCurrentId(mCurrentId);
            cryptogram = getCurrent();
        }
        return cryptogram;
    }

    @NonNull
    public SparseArray<CryptogramProgress> getProgress() {
        if (mCryptogramProgress == null) {
            mCryptogramProgress = new SparseArray<>();
            Set<String> progressStrSet = PrefsUtils.getProgress();
            if (progressStrSet != null) {
                for (String progressStr : progressStrSet) {
                    CryptogramProgress progress = mGson.fromJson(progressStr, CryptogramProgress.class);
                    mCryptogramProgress.put(progress.getId(), progress);
                }
            }
        }
        return mCryptogramProgress;
    }

    public void setProgress(CryptogramProgress progress) {
        SparseArray<CryptogramProgress> progressList = getProgress();
        progressList.put(progress.getId(), progress);

        // Now store everything
        Set<String> progressStrSet = new LinkedHashSet<>();
        for (int i = 0; i < progressList.size(); i++) {
            progressStrSet.add(mGson.toJson(progressList.valueAt(i)));
        }
        PrefsUtils.setProgress(progressStrSet);
    }

}

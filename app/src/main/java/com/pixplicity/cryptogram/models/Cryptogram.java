package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.utils.CryptogramProvider;

import java.util.HashMap;

public class Cryptogram {

    @SerializedName("id")
    private int mId;

    @SerializedName("quote")
    private String mQuote;

    @SerializedName("author")
    private String mAuthor;

    @SerializedName("category")
    private String mCategory;

    private transient String[] mWords;

    private CryptogramProgress mProgress;
    private boolean mLoadedProgress;

    /**
     * Creates a mock cryptogram.
     */
    public Cryptogram() {
        mQuote = "Sample cryptogram; for testing only.";
        mAuthor = "Paul Lammertsma";
        mCategory = "Other";
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getQuote() {
        return mQuote;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getCategory() {
        return mCategory;
    }

    @NonNull
    public String[] getWords() {
        if (mWords == null) {
            mWords = mQuote.split("\\s");
        }
        return mWords;
    }

    public CryptogramProgress getProgress() {
        if (mProgress == null) {
            mProgress = new CryptogramProgress(mId);
        }
        return mProgress;
    }

    @NonNull
    public HashMap<Character, Character> getCharMapping() {
        // Ensure we've attempted to load the data
        load();
        return getProgress().getCharMapping(this);
    }

    public void setCharMapping(HashMap<Character, Character> charMapping) {
        getProgress().setCharMapping(charMapping);
    }

    @NonNull
    private HashMap<Character, Character> getUserChars() {
        load();
        return getProgress().getUserChars(this);
    }

    public Character getUserChar(char c) {
        return getUserChars().get(c);
    }

    public void setUserChar(char selectedCharacter, char c) {
        getUserChars().put(selectedCharacter, Character.toUpperCase(c));
        save();
    }

    public boolean isInputChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private void load() {
        if (!mLoadedProgress) {
            mProgress = CryptogramProvider.getInstance(CryptogramApp.getInstance()).getProgress().get(mId);
        }
        mLoadedProgress = true;
    }

    public void reset() {
        mProgress = null;
        save();
    }

    public void save() {
        CryptogramProvider.getInstance(CryptogramApp.getInstance()).setProgress(getProgress());
    }
}

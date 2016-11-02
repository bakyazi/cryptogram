package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.utils.CryptogramProvider;

import java.util.HashMap;

public class Cryptogram {

    @SerializedName("id")
    private int mId;

    @SerializedName("text")
    private String mText;

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
        mText = "Sample cryptogram; for testing only.";
        mAuthor = "Paul Lammertsma";
        mCategory = "Other";
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getText() {
        return mText;
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
            mWords = mText.split("\\s");
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

    public Character getCharacterForMapping(char c) {
        HashMap<Character, Character> charMapping = getCharMapping();
        for (Character character : charMapping.keySet()) {
            if (charMapping.get(character) == c) {
                return character;
            }
        }
        return null;
    }

    public Character getUserChar(char c) {
        load();
        return getProgress().getUserChar(this, c);
    }

    public void setUserChar(char selectedCharacter, char c) {
        load();
        getProgress().setUserChar(this, selectedCharacter, c);
        save();
    }

    public boolean isInputChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public boolean isCompleted() {
        load();
        return getProgress().isCompleted(this);
    }

    private void load() {
        if (!mLoadedProgress) {
            mProgress = CryptogramProvider.getInstance(CryptogramApp.getInstance()).getProgress().get(mId);
            if (mProgress != null) {
                mProgress.sanitize(this);
            }
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

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
        // Ensure we've attempted to load the data
        load();
        if (mProgress == null) {
            mProgress = new CryptogramProgress(mId);
        }
        return mProgress;
    }

    @NonNull
    public HashMap<Character, Character> getCharMapping() {
        return getProgress().getCharMapping(this);
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
        return getProgress().getUserChar(this, c);
    }

    public void setUserChar(char selectedCharacter, char c) {
        getProgress().setUserChar(this, selectedCharacter, c);
        save();
    }

    public boolean isInputChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public boolean isCompleted() {
        return getProgress().isCompleted(this);
    }

    public void reveal(char c) {
        if (!isInputChar(c)) {
            // Not applicable
            return;
        }
        getProgress().reveal(c);
        save();
    }

    public boolean isRevealed(char c) {
        return getProgress().isRevealed(c);
    }

    public int getReveals() {
        return getProgress().getReveals();
    }

    public int getExcessCount() {
        // TODO
        return 0;
    }

    /**
     * Returns the duration of the user's play time on this puzzle in milliseconds.
     */
    public long getDuration() {
        return getProgress().getDuration();
    }

    public void onResume() {
        getProgress().onResume(this);
    }

    public void onPause() {
        getProgress().onPause(this);
        save();
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
        getProgress().reset(this);
        save();
    }

    public void save() {
        CryptogramProvider.getInstance(CryptogramApp.getInstance()).setProgress(getProgress());
    }

}

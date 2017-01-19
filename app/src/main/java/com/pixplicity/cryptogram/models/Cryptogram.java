package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.utils.CryptogramProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Cryptogram {

    protected boolean mIsMock;

    @SerializedName("id")
    protected int mId;

    @SerializedName("text")
    protected String mText;

    @SerializedName("author")
    protected String mAuthor;

    @SerializedName("topic")
    protected String mTopic;

    private transient String[] mWords;

    private CryptogramProgress mProgress;
    private boolean mLoadedProgress;

    public Cryptogram() {}

    public static class Mock extends Cryptogram {

        /**
         * Creates a mock cryptogram.
         */
        public Mock() {
            mText = "Bright vixens jump; dozy fowl quack.";
            mAuthor = "Paul Lammertsma";
            mTopic = "Other";
            mIsMock = true;
        }

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

    public String getTopic() {
        return mTopic;
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

    public ArrayList<Character> getCharacterList() {
        return getProgress().getCharacterList(this);
    }

    public Character getCharacterForMapping(char mappedChar) {
        return getCharMapping().get(mappedChar);
    }

    public Character getMappedCharacter(char inputChar) {
        HashMap<Character, Character> charMapping = getCharMapping();
        for (Character character : charMapping.keySet()) {
            if (charMapping.get(character) == inputChar) {
                return character;
            }
        }
        return null;
    }

    public Collection<Character> getUserChars() {
        return getProgress().getUserChars(this);
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

    public void revealedMistakes() {
        getProgress().incrementRevealedMistakes();
        save();
    }

    public void revealPuzzle() {
        HashMap<Character, Character> charMapping = getCharMapping();
        for (Character c : charMapping.keySet()) {
            getProgress().setUserChar(this, c, c);
        }
        save();
    }

    public boolean isRevealed(char c) {
        return getProgress().isRevealed(c);
    }

    public int getReveals() {
        return getProgress().getReveals();
    }

    public int getExcessCount() {
        return getProgress().getExcessCount(this);
    }

    /**
     * Returns the duration of the user's play time on this puzzle in milliseconds.
     */
    public long getDuration() {
        return getProgress().getDuration();
    }

    public float getScore() {
        return getProgress().getScore(this);
    }

    public void onResume() {
        getProgress().onResume(this);
    }

    public void onPause() {
        getProgress().onPause();
        save();
    }

    private void load() {
        if (!mLoadedProgress && !mIsMock) {
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
        if (!mIsMock) {
            CryptogramProvider.getInstance(CryptogramApp.getInstance()).setProgress(getProgress());
        }
    }

}

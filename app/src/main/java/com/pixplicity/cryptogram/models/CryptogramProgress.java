package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CryptogramProgress {

    private static final List<Character> ALPHABET = new ArrayList<>(26);

    static {
        for (int i = 'A'; i <= 'Z'; i++) {
            ALPHABET.add((char) i);
        }
    }

    public CryptogramProgress() {
    }

    public CryptogramProgress(int id) {
        mId = id;
    }

    @SerializedName("id")
    private int mId;

    /**
     * Maps from correct characters to user inputted characters.
     */
    @SerializedName("user")
    private HashMap<Character, Character> mUserChars;

    /**
     * Maps from correct characters to randomized characters.
     */
    @SerializedName("shuffle")
    private HashMap<Character, Character> mCharMapping;

    /**
     * Temporary ordered list of characters.
     */
    private transient ArrayList<Character> mCharacterList;

    /**
     * List of characters that have been revealed.
     */
    @SerializedName("revealed")
    private List<Character> mRevealed;

    /**
     * Timestamp of (corrected) puzzle start time.
     */
    @SerializedName("start_time")
    private Long mStartTime;

    /**
     * Timestamp of puzzle completion time, or time at which playing was stopped.
     */
    @SerializedName("stop_time")
    private Long mStopTime;

    /**
     * Total times characters were input by the user.
     */
    @SerializedName("inputs")
    private Integer mInputs;

    private transient Boolean mPlaying;

    private transient Boolean mCompleted;

    public int getId() {
        return mId;
    }

    @NonNull
    public HashMap<Character, Character> getCharMapping(Cryptogram cryptogram) {
        // Ensure we've attempted to load the data
        if (mCharMapping == null) {
            mCharMapping = new HashMap<>();
            mCharacterList = new ArrayList<>();
            for (String word : cryptogram.getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = Character.toUpperCase(word.charAt(i));
                    if (cryptogram.isInputChar(c) && !mCharMapping.containsKey(c)) {
                        mCharMapping.put(c, (char) 0);
                        mCharacterList.add(c);
                    }
                }
            }
            Random r = new Random();
            List<Character> alphabet = new ArrayList<>(ALPHABET.size());
            for (Character c : ALPHABET) {
                alphabet.add(c);
            }
            Collections.shuffle(alphabet, r);
            for (Character c : mCharMapping.keySet()) {
                int i = r.nextInt(alphabet.size());
                char mappedC = alphabet.get(i);
                alphabet.remove(i);
                mCharMapping.put(c, mappedC);
            }
            // We've generated the mappings, save it
            cryptogram.save();
        }
        return mCharMapping;
    }

    public ArrayList<Character> getCharacterList(Cryptogram cryptogram) {
        getCharMapping(cryptogram);
        if (mCharacterList == null) {
            // May need to be regenerated as the field is transient
            mCharacterList = new ArrayList<>();
            for (String word : cryptogram.getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = Character.toUpperCase(word.charAt(i));
                    if (cryptogram.isInputChar(c) && !mCharacterList.contains(c)) {
                        mCharacterList.add(c);
                    }
                }
            }
        }
        return mCharacterList;
    }

    public void setCharMapping(HashMap<Character, Character> charMapping) {
        mCharMapping = charMapping;
    }

    @NonNull
    private HashMap<Character, Character> getUserCharsMapping(Cryptogram cryptogram) {
        if (mUserChars == null) {
            mUserChars = new HashMap<>();
            for (String word : cryptogram.getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = Character.toUpperCase(word.charAt(i));
                    if (cryptogram.isInputChar(c)) {
                        mUserChars.put(c, (char) 0);
                    }
                }
            }
        }
        return mUserChars;
    }

    public Collection<Character> getUserChars(Cryptogram cryptogram) {
        return getUserCharsMapping(cryptogram).values();
    }

    @Nullable
    public Character getUserChar(Cryptogram cryptogram, char c) {
        return getUserCharsMapping(cryptogram).get(c);
    }

    public void setUserChar(Cryptogram cryptogram, char selectedCharacter, char c) {
        mCompleted = null;
        if (mInputs == null) {
            mInputs = 1;
        } else {
            mInputs++;
        }
        getUserCharsMapping(cryptogram).put(selectedCharacter, Character.toUpperCase(c));
    }

    public int getExcessCount(Cryptogram cryptogram) {
        if (mInputs == null) {
            return -1;
        }
        // Start with total number of inputs
        int count = mInputs;
        for (Character c : getUserCharsMapping(cryptogram).values()) {
            if (c != 0) {
                // Subtract any filled in characters
                count--;
            }
        }
        return count;
    }

    public boolean isCompleted(Cryptogram cryptogram) {
        if (mCompleted == null) {
            mCompleted = true;
            HashMap<Character, Character> userChars = getUserCharsMapping(cryptogram);
            for (Character character : userChars.keySet()) {
                // In order to be correct, the key and value must be identical
                if (character != userChars.get(character)) {
                    mCompleted = false;
                    break;
                }
            }
            if (mCompleted) {
                onPause();
            }
        }
        return mCompleted;
    }

    public void reveal(char c) {
        if (mRevealed == null) {
            mRevealed = new ArrayList<>();
        } else if (mRevealed.contains(c)) {
            return;
        }
        mRevealed.add(c);
    }

    public boolean isRevealed(char c) {
        return mRevealed != null && mRevealed.contains(c);
    }

    public int getReveals() {
        return mRevealed == null ? 0 : mRevealed.size();
    }

    public boolean isPlaying() {
        return mPlaying != null && mPlaying;
    }

    public void onResume(Cryptogram cryptogram) {
        if (!isPlaying() && !isCompleted(cryptogram)) {
            // Only resume playing if the puzzle wasn't completed
            setTimes();
            mPlaying = true;
        }
    }

    public void onPause() {
        if (isPlaying()) {
            setTimes();
            mPlaying = false;
        }
    }

    private void setTimes() {
        long stopTime = System.currentTimeMillis();
        mStartTime = stopTime - getDuration();
        mStopTime = stopTime;
    }

    public long getDuration() {
        if (mStartTime == null || mStartTime == 0) {
            return 0;
        }
        if (!isPlaying()) {
            if (mStopTime == null || mStopTime == 0) {
                return 0;
            }
            return mStopTime - mStartTime;
        }
        return System.currentTimeMillis() - mStartTime;
    }

    public void sanitize(Cryptogram cryptogram) {
        // Ensure that only input characters have user mappings
        Iterator<Character> i = getUserCharsMapping(cryptogram).keySet().iterator();
        while (i.hasNext()) {
            Character c = i.next();
            if (!cryptogram.isInputChar(c)) {
                i.remove();
            }
        }
        // Apply mappings for any revealed characters
        if (mRevealed != null) {
            for (Character c : mRevealed) {
                setUserChar(cryptogram, c, c);
            }
        }
    }

    public void reset(Cryptogram cryptogram) {
        mUserChars = null;
        mCharMapping = null;
        mStartTime = null;
        mStopTime = null;
        mCompleted = null;
        if (isPlaying()) {
            mPlaying = null;
            onResume(cryptogram);
        }
        sanitize(cryptogram);
    }

}

package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
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

    private transient Boolean mCompleted;

    public int getId() {
        return mId;
    }

    @NonNull
    public HashMap<Character, Character> getCharMapping(Cryptogram cryptogram) {
        // Ensure we've attempted to load the data
        if (mCharMapping == null) {
            mCharMapping = new HashMap<>();
            for (String word : cryptogram.getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = Character.toUpperCase(word.charAt(i));
                    if (cryptogram.isInputChar(c)) {
                        mCharMapping.put(c, (char) 0);
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

    public void setCharMapping(HashMap<Character, Character> charMapping) {
        mCharMapping = charMapping;
    }

    @NonNull
    private HashMap<Character, Character> getUserChars(Cryptogram cryptogram) {
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

    @Nullable
    public Character getUserChar(Cryptogram cryptogram, char c) {
        return getUserChars(cryptogram).get(c);
    }

    public void setUserChar(Cryptogram cryptogram, char selectedCharacter, char c) {
        mCompleted = null;
        getUserChars(cryptogram).put(selectedCharacter, Character.toUpperCase(c));
    }

    public void sanitize(Cryptogram cryptogram) {
        Iterator<Character> i = getUserChars(cryptogram).keySet().iterator();
        while (i.hasNext()) {
            Character c = i.next();
            if (!cryptogram.isInputChar(c)) {
                i.remove();
            }
        }
    }

    public boolean isCompleted(Cryptogram cryptogram) {
        if (mCompleted == null) {
            mCompleted = true;
            HashMap<Character, Character> userChars = getUserChars(cryptogram);
            for (Character character : userChars.keySet()) {
                // In order to be correct, the key and value must be identical
                if (character != userChars.get(character)) {
                    mCompleted = false;
                    break;
                }
            }
        }
        return mCompleted;
    }

}

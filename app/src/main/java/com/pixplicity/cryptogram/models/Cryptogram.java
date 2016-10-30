package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Cryptogram {

    private static final List<Character> ALPHABET = new ArrayList<>(26);

    static {
        for (int i = 'A'; i <= 'Z'; i++) {
            ALPHABET.add((char) i);
        }
    }

    @SerializedName("quote")
    private String mQuote;

    @SerializedName("author")
    private String mAuthor;

    @SerializedName("category")
    private String mCategory;

    private HashMap<Character, Character> mCharMapping;

    /**
     * Creates a mock cryptogram.
     */
    public Cryptogram() {
        mQuote = "Sample cryptogram; for testing only.";
        mAuthor = "Paul Lammertsma";
        mCategory = "Other";
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

    public String[] getWords() {
        return mQuote.split("\\s");
    }

    @NonNull
    public HashMap<Character, Character> getCharMapping() {
        if (mCharMapping == null) {
            mCharMapping = new HashMap<>();
            for (String word : getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = Character.toUpperCase(word.charAt(i));
                    if (isInputChar(c)) {
                        mCharMapping.put(c, (char) 0);
                    }
                }
            }
            Random r = new Random(100);
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
        }
        return mCharMapping;
    }

    public boolean isInputChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

}

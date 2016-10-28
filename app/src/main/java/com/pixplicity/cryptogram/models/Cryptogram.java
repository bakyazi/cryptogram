package com.pixplicity.cryptogram.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Random;

public class Cryptogram {

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

    public HashMap<Character, Character> getCharMapping() {
        if (mCharMapping == null) {
            mCharMapping = new HashMap<>();
            for (String word : getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = word.charAt(i);
                    if (isInputChar(c)) {
                        mCharMapping.put(c, (char) 0);
                    }
                }
            }
            char mappedC = (char) ('A' + new Random().nextInt(26));
            for (Character c : mCharMapping.keySet()) {
                if (mappedC > 'Z') mappedC = 'A';
                mCharMapping.put(c, mappedC);
                mappedC++;
            }
        }
        return mCharMapping;
    }

    public boolean isInputChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

}

package com.pixplicity.cryptogram.models;

import com.google.gson.annotations.SerializedName;

public class Cryptogram {

    @SerializedName("quote")
    private String mQuote;

    @SerializedName("author")
    private String mAuthor;

    @SerializedName("category")
    private String mCategory;

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

}

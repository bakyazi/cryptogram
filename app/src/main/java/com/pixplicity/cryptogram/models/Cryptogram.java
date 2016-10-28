package com.pixplicity.cryptogram.models;

import com.google.gson.annotations.SerializedName;

public class Cryptogram {

    @SerializedName("quote")
    private String mQuote;

    @SerializedName("author")
    private String mAuthor;

    @SerializedName("category")
    private String mCategory;

    public String getQuote() {
        return mQuote;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getCategory() {
        return mCategory;
    }

}

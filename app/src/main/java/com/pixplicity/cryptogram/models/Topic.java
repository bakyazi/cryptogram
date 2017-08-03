package com.pixplicity.cryptogram.models;

import com.google.gson.annotations.SerializedName;

public class Topic {

    @SerializedName("name")
    protected String mName;

    @SerializedName("description")
    protected String mDescription;

    @SerializedName("topics")
    protected String mTopics;

    private transient String[] mTopicNames;

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String[] getTopics() {
        if (mTopicNames == null) {
            mTopicNames = mTopics.split("[|]");
        }
        return mTopicNames;
    }

}

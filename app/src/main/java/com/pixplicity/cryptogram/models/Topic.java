package com.pixplicity.cryptogram.models;

import com.google.gson.annotations.SerializedName;

public class Topic {

    @SerializedName("id")
    protected String mId;

    @SerializedName("name")
    protected String mName;

    @SerializedName("cover")
    protected String mCover;

    @SerializedName("description")
    protected String mDescription;

    @SerializedName("topics")
    protected String mTopics;

    private transient String[] mTopicNames;

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getCover() {
        return mCover;
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

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Topic topic = (Topic) o;

        return mId != null ? mId.equals(topic.mId) : topic.mId == null;
    }

    @Override
    public int hashCode() {
        return mId != null ? mId.hashCode() : 0;
    }

}

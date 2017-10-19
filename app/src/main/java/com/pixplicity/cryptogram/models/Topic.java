package com.pixplicity.cryptogram.models;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.pixplicity.cryptogram.providers.PuzzleProvider;

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

    @SerializedName("puzzles")
    protected Puzzle[] mPuzzles;

    private transient String[] mTopicNames;

    private transient Puzzle[] mPuzzles;

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

    public Puzzle[] getPuzzles() {
        return mPuzzles;
    }

    public void setPuzzles(Puzzle[] puzzles) {
        mPuzzles = puzzles;
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

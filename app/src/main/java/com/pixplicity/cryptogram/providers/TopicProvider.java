package com.pixplicity.cryptogram.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.models.Topic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public class TopicProvider extends AssetProvider {

    private static final String ASSET_FILENAME = "topics.json";

    private static TopicProvider sInstance;

    private Map<String, Topic> mTopics;

    @NonNull
    public static TopicProvider getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TopicProvider(context);
        }
        return sInstance;
    }

    private TopicProvider(@Nullable Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected String getAssetFilename() {
        return ASSET_FILENAME;
    }

    @Override
    protected void onLoadFailure(Context context, IOException e) {
        // Ignore
    }

    @Override
    protected void onLoad(Context context, InputStream is) {
        Type type = new TypeToken<Map<String, Topic>>() {
        }.getType();
        mTopics = GsonProvider.getGson().fromJson(new InputStreamReader(is), type);
        for (String topicId : mTopics.keySet()) {
            Topic topic = mTopics.get(topicId);
            Puzzle[] puzzles = PuzzleProvider.getInstance(context).getAllForTopic(topic);
            topic.setPuzzles(puzzles);
        }
    }

    public Map<String, Topic> getTopics() {
        return mTopics;
    }

    @Nullable
    public Topic getTopicById(@Nullable String topicId) {
        if (topicId != null) {
            mTopics.get(topicId);
        }
        return null;
    }

}

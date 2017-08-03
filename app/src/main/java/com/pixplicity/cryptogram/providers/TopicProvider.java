package com.pixplicity.cryptogram.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pixplicity.cryptogram.models.Topic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TopicProvider extends AssetProvider {

    private static final String ASSET_FILENAME = "topics.json";

    private static TopicProvider sInstance;

    private Topic[] mTopics;

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
        mTopics = GsonProvider.getGson().fromJson(new InputStreamReader(is), Topic[].class);
    }

    public Topic[] getTopics() {
        return mTopics;
    }

}

package com.pixplicity.cryptogram.providers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.pixplicity.cryptogram.utils.Logger;

import java.io.IOException;
import java.io.InputStream;

public abstract class AssetProvider {

    public AssetProvider(Context context) {
        final String assetFilename = getAssetFilename();
        try {
            if (context != null) {
                InputStream is = context.getAssets().open(assetFilename);
                onLoad(context, is);
            } else {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("assets/" + assetFilename);
                if (is != null) {
                    onLoad(context, is);
                    is.close();
                }
            }
        } catch (IOException e) {
            Logger.e("parsing", "could not read asset " + assetFilename, e);
            onLoadFailure(context, e);
        }
    }

    @NonNull
    protected abstract String getAssetFilename();

    protected abstract void onLoad(Context context, InputStream is);

    protected abstract void onLoadFailure(Context context, IOException e);

}

package com.pixplicity.cryptogram.utils;

import android.content.res.Resources;
import android.support.annotation.DimenRes;

public class StyleUtils {

    private static Float sSizeMultiplier;

    private StyleUtils() {
    }

    private static float getSizeMultiplier() {
        if (sSizeMultiplier == null) {
            sSizeMultiplier = (float) Math.pow(1.25f, PrefsUtils.getTextSize());
        }
        return sSizeMultiplier;
    }

    public static void reset() {
        sSizeMultiplier = null;
    }

    public static int getSize(Resources res, @DimenRes int dimensionId) {
        final int textSize = res.getDimensionPixelSize(dimensionId);
        return Math.round(textSize * getSizeMultiplier());
    }

}

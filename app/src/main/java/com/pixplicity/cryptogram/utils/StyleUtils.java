package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.util.TypedValue;

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

    @ColorInt
    public static int getColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

}

package com.pixplicity.cryptogram.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.TextView;

public class TintUtils {

    public static void tint(@ColorInt int color, @NonNull TextView textView) {
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        for (int i = 0; i < compoundDrawables.length; i++) {
            compoundDrawables[i] = DrawableCompat.wrap(compoundDrawables[i]);
            if (compoundDrawables[i] != null) {
                DrawableCompat.setTint(compoundDrawables[i], color);
            }
        }
        textView.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
    }

}

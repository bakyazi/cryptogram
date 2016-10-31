package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;

public class HtmlCompat {

    @NonNull
    public static Spanned fromHtml(@NonNull String source) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(source);
        }
    }

    @NonNull
    public static Spanned fromHtml(@NonNull Context context, @StringRes int strRes) {
        return fromHtml(context.getString(strRes));
    }

}

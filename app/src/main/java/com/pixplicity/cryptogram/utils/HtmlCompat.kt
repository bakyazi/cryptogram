package com.pixplicity.cryptogram.utils

import android.content.Context
import android.support.annotation.StringRes
import android.text.Html
import android.text.Spanned

object HtmlCompat {

    fun fromHtml(source: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {

            Html.fromHtml(source)
        }
    }

    fun fromHtml(context: Context, @StringRes strRes: Int): Spanned {
        return fromHtml(context.getString(strRes))
    }

}

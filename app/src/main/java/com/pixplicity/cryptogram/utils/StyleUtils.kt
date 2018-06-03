package com.pixplicity.cryptogram.utils

import android.content.Context
import android.content.res.Resources
import android.support.annotation.ColorInt
import android.support.annotation.DimenRes
import android.util.TypedValue

object StyleUtils {

    private var sizeMultiplier: Float? = null
        get() {
            if (field == null) {
                var textSize: Float
                try {
                    textSize = PrefsUtils.textSize.toFloat()
                } catch (e: Exception) {
                    textSize = 1f
                }

                field = Math.pow(1.25, textSize.toDouble()).toFloat()
            }
            return field
        }

    fun reset() {
        sizeMultiplier = null
    }

    fun getSize(res: Resources, @DimenRes dimensionId: Int): Int {
        val textSize = res.getDimensionPixelSize(dimensionId)
        return Math.round(textSize * (sizeMultiplier ?: 1f))
    }

    @ColorInt
    fun getColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

}

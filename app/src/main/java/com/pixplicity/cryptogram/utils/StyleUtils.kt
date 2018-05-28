package com.pixplicity.cryptogram.utils

import android.content.Context
import android.content.res.Resources
import android.support.annotation.ColorInt
import android.support.annotation.DimenRes
import android.util.TypedValue

object StyleUtils {

    private var sSizeMultiplier: Float? = null

    private val sizeMultiplier: Float
        get() {
            if (sSizeMultiplier == null) {
                var textSize: Float
                try {
                    textSize = PrefsUtils.textSize.toFloat()
                } catch (e: Exception) {
                    textSize = 1f
                }

                sSizeMultiplier = Math.pow(1.25, textSize.toDouble()).toFloat()
            }
            return sSizeMultiplier!!
        }

    fun reset() {
        sSizeMultiplier = null
    }

    fun getSize(res: Resources, @DimenRes dimensionId: Int): Int {
        val textSize = res.getDimensionPixelSize(dimensionId)
        return Math.round(textSize * sizeMultiplier)
    }

    @ColorInt
    fun getColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

}

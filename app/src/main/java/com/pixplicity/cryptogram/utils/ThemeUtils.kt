package com.pixplicity.cryptogram.utils

import android.graphics.ColorMatrixColorFilter
import android.widget.Button
import android.widget.ImageView

/**
 * Color matrix that flips the components (`-1.0f * c + 255 = 255 - c`)
 * and keeps the alpha intact.
 */
private val NEGATIVE = ColorMatrixColorFilter(
        floatArrayOf(-1.0f, 0f, 0f, 0f, 255f, // red
                0f, -1.0f, 0f, 0f, 255f, // green
                0f, 0f, -1.0f, 0f, 255f, // blue
                0f, 0f, 0f, 1.0f, 0f  // alpha
        )
)

fun Button.invertedTheme() {
    val drawables = compoundDrawables
    drawables.forEach {
        if (it != null) {
            it.colorFilter = NEGATIVE
        }
    }
    setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
}

fun ImageView.invertedTheme() {
    drawable.colorFilter = NEGATIVE
}

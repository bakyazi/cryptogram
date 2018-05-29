package com.pixplicity.cryptogram.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.events.PuzzleEvent
import com.pixplicity.cryptogram.utils.EventProvider
import com.pixplicity.cryptogram.utils.KeyboardUtils
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.squareup.otto.Subscribe


class KeyboardImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.keyboardImageButtonStyle) : AppCompatImageButton(context, attrs, defStyleAttr), KeyboardUtils.Contract {

    override var keyIndex: Int = 0
    private var mAlpha = 255

    init {

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.KeyboardImageButton,
                defStyleAttr,
                R.style.KeyboardButton_Image)

        try {
            keyIndex = a.getInteger(R.styleable.KeyboardImageButton_key, 0)
        } finally {
            a.recycle()
        }

        setOnClickListener { view -> KeyboardUtils.dispatch(this) }
        val drawable: Drawable? = ContextCompat.getDrawable(getContext(), KeyboardUtils.getKeyIcon(this))
        drawable!!.alpha = mAlpha
        setImageDrawable(drawable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventProvider.bus.register(this)
    }

    override fun onDetachedFromWindow() {
        EventProvider.bus.unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe
    fun onPuzzleProgress(event: PuzzleEvent.PuzzleProgressEvent) {
        var input = false
        if (PrefsUtils.showUsedChars) {
            val keyText = KeyboardUtils.getKeyText(this)
            if (keyText != null && keyText.isNotEmpty()) {
                event.puzzle?.let {
                    input = it.isUserCharInput(keyText[0])
                }
            }
        }
        mAlpha = if (input) KeyboardUtils.Contract.Companion.ALPHA_GREYED else 255
        invalidate()
    }

}

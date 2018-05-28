package com.pixplicity.cryptogram.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout


class CryptogramLayout : LinearLayout {

    private var mCryptogramView: CryptogramView? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        isClickable = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> if (mCryptogramView != null) {
                mCryptogramView!!.hideSoftInput()
                // Clear selected character
                mCryptogramView!!.selectedCharacter = 0.toChar()
            }
        }
        return super.onTouchEvent(event)
    }

    fun setCryptogramView(view: CryptogramView) {
        mCryptogramView = view
    }

}

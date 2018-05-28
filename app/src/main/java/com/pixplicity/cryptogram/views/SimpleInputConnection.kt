package com.pixplicity.cryptogram.views

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype

class SimpleInputConnection(private val mCryptogramView: CryptogramView) : BaseInputConnection(mCryptogramView, true) {
    private var mLastComposition: String? = null

    override fun getEditable(): Editable {
        return super.getEditable()
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        mCryptogramView.onKeyPress(0.toChar())
        return false
    }

    override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
        var input = text.toString()
        if (input != mLastComposition) {
            input = input.trim { it <= ' ' }
            if (input.length > 0) {
                mCryptogramView.onKeyPress(input[input.length - 1])
            } else {
                mCryptogramView.onKeyPress(0.toChar())
            }
        }
        finishComposingText()
        return true
    }

    override fun setComposingText(text: CharSequence, newCursorPosition: Int): Boolean {
        commitText(text, newCursorPosition)
        mLastComposition = text.toString()
        return true
    }

    override fun finishComposingText(): Boolean {
        return super.finishComposingText()
    }

    companion object {

        val INPUT_TYPE = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        val INPUT_TYPE_FOR_FAULTY_IME = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        val INPUT_NONE = InputType.TYPE_NULL
        val DISABLE_PERSONALIZED_LEARNING = false

        fun hasFaultyIme(context: Context): Boolean {
            val ime = getIme(context)
            if (ime != null) {
                when (ime.packageName) {
                    "com.google.android.inputmethod.latin" -> return true
                }
            }
            return false
        }

        fun getIme(context: Context): InputMethodInfo? {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val ims = imm.currentInputMethodSubtype
            if (ims != null) {
                for (imi in imm.enabledInputMethodList) {
                    if (imi == null) {
                        continue
                    }
                    for (i in 0 until imi.subtypeCount) {
                        if (ims == imi.getSubtypeAt(i)) {
                            return imi
                        }
                    }
                }
            }
            return null
        }
    }

}

package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

public class SimpleInputConnection extends BaseInputConnection {

    public static final int INPUT_TYPE = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
    public static final int INPUT_TYPE_FOR_FAULTY_IME = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    public static final int INPUT_NONE = InputType.TYPE_NULL;
    public static final boolean DISABLE_PERSONALIZED_LEARNING = false;

    private final CryptogramView mCryptogramView;
    private String mLastComposition;

    public SimpleInputConnection(CryptogramView cryptogramView) {
        super(cryptogramView, true);
        mCryptogramView = cryptogramView;
    }

    public static boolean hasFaultyIme(Context context) {
        final InputMethodInfo ime = getIme(context);
        if (ime != null) {
            switch (ime.getPackageName()) {
                case "com.google.android.inputmethod.latin":
                    return true;
            }
        }
        return false;
    }

    @Nullable
    public static InputMethodInfo getIme(Context context) {
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        final InputMethodSubtype ims = imm.getCurrentInputMethodSubtype();
        if (ims != null) {
            for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
                if (imi == null) {
                    continue;
                }
                for (int i = 0; i < imi.getSubtypeCount(); i++) {
                    if (ims.equals(imi.getSubtypeAt(i))) {
                        return imi;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Editable getEditable() {
        return super.getEditable();
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        mCryptogramView.onKeyPress((char) 0);
        return false;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        String input = text.toString();
        if (!input.equals(mLastComposition)) {
            input = input.trim();
            if (input.length() > 0) {
                mCryptogramView.onKeyPress(input.charAt(input.length() - 1));
            } else {
                mCryptogramView.onKeyPress((char) 0);
            }
        }
        finishComposingText();
        return true;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        commitText(text, newCursorPosition);
        mLastComposition = text.toString();
        return true;
    }

    @Override
    public boolean finishComposingText() {
        return super.finishComposingText();
    }

}

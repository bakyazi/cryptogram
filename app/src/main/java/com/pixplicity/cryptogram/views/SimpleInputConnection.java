package com.pixplicity.cryptogram.views;

import android.text.Editable;
import android.text.InputType;
import android.view.inputmethod.BaseInputConnection;

public class SimpleInputConnection extends BaseInputConnection {

    public static final int INPUT_TYPE = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

    private final CryptogramView mCryptogramView;
    private String mLastComposition;

    public SimpleInputConnection(CryptogramView cryptogramView) {
        super(cryptogramView, true);
        mCryptogramView = cryptogramView;
    }

    @Override
    public Editable getEditable() {
        Editable editable = super.getEditable();
        return editable;
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

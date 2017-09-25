package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.KeyboardUtils;


public class KeyboardImageButton extends AppCompatImageButton implements KeyboardUtils.Contract {

    private int mKeycode;

    public KeyboardImageButton(Context context) {
        this(context, null);
    }

    public KeyboardImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardImageButtonStyle);
    }

    public KeyboardImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.KeyboardImageButton,
                defStyleAttr,
                R.style.KeyboardButton_Image);

        try {
            mKeycode = a.getInteger(R.styleable.KeyboardImageButton_key, 0);
        } finally {
            a.recycle();
        }

        setOnClickListener(view -> KeyboardUtils.dispatch(this));
        setImageDrawable(ContextCompat.getDrawable(getContext(), KeyboardUtils.getKeyIcon(this)));
    }

    @Override
    public int getKeyIndex() {
        return mKeycode;
    }

}

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
        super(context);
        init(context, null, 0);
    }

    public KeyboardImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public KeyboardImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.KeyboardImageButton,
                defStyleAttr, 0);

        try {
            mKeycode = a.getInteger(R.styleable.KeyboardImageButton_key, 0);
        } finally {
            a.recycle();
        }

        setOnClickListener(view -> KeyboardUtils.dispatch(KeyboardImageButton.this));
        setImageDrawable(ContextCompat.getDrawable(getContext(), KeyboardUtils.getKeyIcon(this)));
    }

    @Override
    public int getKeyIndex() {
        return mKeycode;
    }

}

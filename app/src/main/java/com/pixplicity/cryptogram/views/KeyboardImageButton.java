package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

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
        final Drawable drawable;
        if (false) {
            drawable = ResourcesCompat.getDrawable(getResources(), KeyboardUtils.getKeyIcon(this), null);
        } else if (false) {
            drawable = ResourcesCompat.getDrawableForDensity(getResources(), KeyboardUtils.getKeyIcon(this), DisplayMetrics.DENSITY_XXXHIGH, null);
        } else {
            drawable = ContextCompat.getDrawable(getContext(), KeyboardUtils.getKeyIcon(this));
        }
        setImageDrawable(drawable);
    }

    @Override
    public int getKeyIndex() {
        return mKeycode;
    }

}

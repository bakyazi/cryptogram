package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.KeyboardUtils;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.squareup.otto.Subscribe;


public class KeyboardImageButton extends AppCompatImageButton implements KeyboardUtils.Contract {

    private int mKeycode;
    private int mAlpha = 255;

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
        drawable = ContextCompat.getDrawable(getContext(), KeyboardUtils.getKeyIcon(this));
        drawable.setAlpha(mAlpha);
        setImageDrawable(drawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventProvider.getBus().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        EventProvider.getBus().unregister(this);
        super.onDetachedFromWindow();
    }

    @Override
    public int getKeyIndex() {
        return mKeycode;
    }

    @Subscribe
    public void onPuzzleProgress(PuzzleEvent.PuzzleProgressEvent event) {
        boolean input = false;
        if (PrefsUtils.getShowHints()) {
            String keyText = KeyboardUtils.getKeyText(this);
            if (keyText != null && keyText.length() > 0) {
                Puzzle puzzle = event.getPuzzle();
                input = puzzle.isUserCharInput(keyText.charAt(0));
            }
        }
        mAlpha = input ? ALPHA_GREYED : 255;
        invalidate();
    }

}

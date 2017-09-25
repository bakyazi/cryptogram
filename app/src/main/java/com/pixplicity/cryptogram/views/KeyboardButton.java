package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.KeyboardUtils;


public class KeyboardButton extends AppCompatButton implements KeyboardUtils.Contract {

    private boolean mShowLetter;

    private int mKeyValue;
    private final Paint mPaint;

    public KeyboardButton(Context context) {
        this(context, null);
    }

    public KeyboardButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardButtonStyle);
    }

    public KeyboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.KeyboardButton,
                defStyleAttr,
                R.style.KeyboardButton);

        try {
            mKeyValue = a.getInteger(R.styleable.KeyboardButton_key, 0);
        } finally {
            a.recycle();
        }

        setOnClickListener(view -> KeyboardUtils.dispatch(this));
        setOnTouchListener((view, motionEvent) -> {
            final int action = motionEvent.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Draw letter press
                    mShowLetter = true;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    performClick();
                case MotionEvent.ACTION_CANCEL:
                    // Conceal letter press
                    mShowLetter = false;
                    invalidate();
                    break;
            }
            return true;
        });
        setText(KeyboardUtils.getKeyText(this));
    }

    @Override
    public int getKeyIndex() {
        return mKeyValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowLetter) {
            // TODO draw something beautiful
            //canvas.drawCircle(canvas.getWidth() / 2, -100, 100, mPaint);
        }
    }

}

package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatButton;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.KeyboardUtils;


public class KeyboardButton extends AppCompatButton implements KeyboardUtils.Contract {

    private static final String TAG = KeyboardButton.class.getSimpleName();

    private boolean mShowLetter;

    private int mKeyValue;

    private final Path mPath = new Path();
    private final Paint mPathPaint;
    private final TextPaint mTextPaint;
    private Rect mViewBounds = new Rect();
    private int mBoxPadding;
    private Rect mBox = new Rect();

    public KeyboardButton(Context context) {
        this(context, null);
    }

    public KeyboardButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardButtonStyle);
    }

    public KeyboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Get theme color
        @ColorInt int colorBg, colorFg;
        {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.keyboardBackground2, typedValue, true);
            colorBg = typedValue.data;
        }
        {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.keyboardForeground2, typedValue, true);
            colorFg = typedValue.data;
        }

        mPathPaint = new Paint();
        mPathPaint.setColor(colorBg);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.FILL);
        mTextPaint = new TextPaint(mPathPaint);
        mTextPaint.setColor(colorFg);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.keyboard_popup_text_size));
        mPathPaint.setShadowLayer(16.0f, 0.0f, 2.0f, Color.argb(50, 0, 0, 0));

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
                case MotionEvent.ACTION_MOVE:
                    boolean show = isViewInBounds(motionEvent.getX(), motionEvent.getY());
                    if (mShowLetter != show) {
                        mShowLetter = show;
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isViewInBounds(motionEvent.getX(), motionEvent.getY())) {
                        performClick();
                    }
                case MotionEvent.ACTION_CANCEL:
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

    private boolean isViewInBounds(float x, float y) {
        return mViewBounds.contains(Math.round(x), Math.round(y));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "onLayout: " + KeyboardUtils.getKeyText(this) + " " + left + "-" + right);

        // Store view dimensions
        getDrawingRect(mViewBounds);

        // Store path for touch
        int parentWidth = ((View) getParent()).getWidth();
        int width = right - left;
        mBoxPadding = getResources().getDimensionPixelSize(R.dimen.keyboard_popup_padding);
        {
            int boxWidth = getResources().getDimensionPixelSize(R.dimen.keyboard_popup_width);
            int boxHeight = getResources().getDimensionPixelSize(R.dimen.keyboard_popup_height);
            int boxLeft = Math.max(-left, Math.min(parentWidth - right, width / 2 - boxWidth / 2));
            mBox.left = boxLeft;
            mBox.top = -boxHeight;
            mBox.right = boxLeft + boxWidth;
            mBox.bottom = 0;
        }
        mPath.moveTo(mBoxPadding, mBoxPadding);
        mPath.lineTo(mBoxPadding, 0);
        {
            // Box itself
            // TODO this would be nicer: mPath.quadTo()
            mPath.lineTo(mBox.left, mBox.bottom);
            mPath.lineTo(mBox.left, mBox.top);
            mPath.lineTo(mBox.right, mBox.top);
            mPath.lineTo(mBox.right, mBox.bottom);
        }
        mPath.lineTo(width - mBoxPadding, 0);
        mPath.lineTo(width - mBoxPadding, mBoxPadding);
        mPath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowLetter) {
            canvas.drawPath(mPath, mPathPaint);
            int x = mBox.left + (mBox.right - mBox.left) / 2;
            int y = mBox.top + (int) ((mBox.bottom - mBox.top) / 2 - (mTextPaint.descent() + mTextPaint.ascent()) / 2);
            canvas.drawText(KeyboardUtils.getKeyText(this), x, y, mTextPaint);
        }
    }

}
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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.KeyboardUtils;
import com.pixplicity.cryptogram.utils.Logger;

public class KeyboardButton extends AppCompatButton implements KeyboardUtils.Contract {

    private boolean mShowLetter;

    private int mKeyValue;

    private final Path mPath = new Path();
    private final Paint mPathPaint;
    private final TextPaint mTextPaint;
    private Rect mViewBounds = new Rect();
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
        mPathPaint.setShadowLayer(4.0f, 0.0f, 4.0f, Color.argb(200, 0, 0, 0));

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

        // Store view dimensions
        getDrawingRect(mViewBounds);

        // Store path for touch
        int parentWidth = ((View) getParent()).getWidth();
        int width = right - left;
        int boxPadding = getResources().getDimensionPixelSize(R.dimen.keyboard_popup_padding);
        {
            int boxWidth = getResources().getDimensionPixelSize(R.dimen.keyboard_popup_width);
            int boxHeight = getResources().getDimensionPixelSize(R.dimen.keyboard_popup_height);
            int boxLeft = Math.max(-left, Math.min(parentWidth - left - boxWidth, width / 2 - boxWidth / 2));
            if (mKeyValue == 16 || mKeyValue == 17) {
                Logger.d("layout", mKeyValue + "; " + parentWidth + " - " + right);
            }
            mBox.left = boxLeft;
            mBox.top = -boxHeight;
            mBox.right = boxLeft + boxWidth;
            mBox.bottom = 0;
        }
        int x1 = Math.max(mBox.left + boxPadding, boxPadding);
        int x2 = Math.min(mBox.right - boxPadding, width - boxPadding);
        // Inset below box
        mPath.moveTo(x1, boxPadding);
        mPath.lineTo(x1, 0);
        {
            // Box itself
            // TODO mPath.quadTo() would be nicer
            mPath.lineTo(mBox.left, mBox.bottom);
            mPath.lineTo(mBox.left, mBox.top);
            mPath.lineTo(mBox.right, mBox.top);
            mPath.lineTo(mBox.right, mBox.bottom);
        }
        // Return to inset below box
        mPath.lineTo(x2, 0);
        mPath.lineTo(x2, boxPadding);
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

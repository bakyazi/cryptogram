package com.pixplicity.cryptogram.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;


public class HintView extends TextView {

    private static final String TAG = HintView.class.getSimpleName();

    @Nullable
    private Cryptogram mCryptogram;

    private float mBoxW, mCharH, mCharW;
    private TextPaint mTextPaint;


    public HintView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public HintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public HintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Resources r = context.getResources();

        mTextPaint = new TextPaint();
        mTextPaint.setColor(getCurrentTextColor());
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.MONOSPACE);

        // Compute size of each box
        mBoxW = r.getDimensionPixelSize(R.dimen.puzzle_box_width);
        mTextPaint.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_hint_size));

        // Compute size of a single char (assumes monospaced font!)
        Rect bounds = new Rect();
        mTextPaint.getTextBounds("M", 0, 1, bounds);
        mCharW = bounds.width();
        mCharH = bounds.height();

        if (isInEditMode()) {
            setCryptogram(new Cryptogram());
        }
    }

    @Nullable
    public Cryptogram getCryptogram() {
        return mCryptogram;
    }

    public void setCryptogram(Cryptogram cryptogram) {
        mCryptogram = cryptogram;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = getSuggestedMinimumWidth();

        int width;
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        int desiredHeight = drawChars(null, width);

        int height;
        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth() - getPaddingRight();
        drawChars(canvas, width);
    }

    private int drawChars(@Nullable Canvas canvas, int width) {
        int charsPerRow = 1;
        int innerBox = width - getPaddingLeft();
        float boxW = mBoxW;
        for (int i = 1; i < 26; i++) {
            charsPerRow = (int) Math.ceil(26f / i);
            boxW = innerBox / charsPerRow;
            if (boxW >= mBoxW) {
                break;
            }
        }

        int desiredHeight = getPaddingTop();

        if (mCryptogram != null) {
            // Compute the height that works for this width
            float offsetY = mCharH / 2;
            float offsetX = (boxW / 2) - (mCharW / 2);
            float x = getPaddingLeft(), y = getPaddingTop() + mCharH;
            char c = 'A';
            for (int i = 0; i < 26; i++) {
                if (i % charsPerRow == 0) {
                    x = getPaddingLeft();
                    if (i > 0) {
                        y += mCharH + offsetY;
                    }
                } else {
                    // Box width
                    x += boxW;
                }
                if (canvas != null) {
                    String chr = String.valueOf(c);
                    // Check if it's been mapped already
                    Character userChar = mCryptogram.getUserChar(c);
                    Log.d(TAG, chr + " --> " + userChar);
                    if (userChar != null && userChar != 0) {
                        mTextPaint.setAlpha(96);
                    } else {
                        mTextPaint.setAlpha(255);
                    }
                    // Draw the character
                    canvas.drawText(chr, x + offsetX, y, mTextPaint);
                }
                c++;
            }
            desiredHeight = (int) (y);
        }

        desiredHeight += getPaddingBottom();
        return desiredHeight;
    }

}

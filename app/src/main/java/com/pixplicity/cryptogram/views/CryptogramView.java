package com.pixplicity.cryptogram.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;


public class CryptogramView extends View {

    private Cryptogram mCryptogram;
    private String[] mWords;

    private float mBoxW, mBoxH, mCharW, mCharH;
    private Paint mPaint;
    private TextPaint mTextPaint;


    public CryptogramView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public CryptogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public CryptogramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CryptogramView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);

        mTextPaint = new TextPaint(mPaint);
        mTextPaint.setTypeface(Typeface.MONOSPACE);

        // Compute size of each box
        Resources r = context.getResources();
        mBoxW = r.getDimensionPixelSize(R.dimen.puzzle_box_width);
        mBoxH = r.getDimensionPixelSize(R.dimen.puzzle_box_height);
        mTextPaint.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_text_size));

        // Compute size of a single char (assumes monospaced font!)
        Rect bounds = new Rect();
        mTextPaint.getTextBounds("M", 0, 1, bounds);
        mCharW = bounds.width();
        mCharH = bounds.height();

        if (isInEditMode()) {
            setCryptogram(new Cryptogram());
        }
    }

    public void setCryptogram(Cryptogram cryptogram) {
        mCryptogram = cryptogram;
        mWords = mCryptogram.getWords();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float offsetX = (mBoxW - mCharW) / 2;
        float offsetY = mBoxH / 4;
        float x = 0, y = mBoxH;
        for (String word : mWords) {
            float w = word.length() * mBoxW;
            if (x + w > canvas.getWidth()) {
                x = 0;
                y += mBoxH * 2 + offsetY * 2;
            }
            for (int i = 0; i < word.length(); i++) {
                String chr = String.valueOf(word.charAt(i));
                canvas.drawText(chr, x + offsetX, y, mTextPaint);
                canvas.drawLine(x, y + offsetY, x + mBoxW, y + offsetY, mPaint);
                // Box width
                x += mBoxW;
            }
            // Trailing space
            x += mBoxW;
        }
    }

}

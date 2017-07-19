package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.StyleUtils;

import java.util.Collection;


public class HintView extends AppCompatTextView {

    private static final String TAG = HintView.class.getSimpleName();

    @Nullable
    private Puzzle mPuzzle;

    private int mCharsPerRow = 1;

    private float mMinBoxW, mBoxW, mCharH, mCharW;
    private TextPaint mTextPaint;


    public HintView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public HintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public HintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Resources res = context.getResources();

        mTextPaint = new TextPaint();
        mTextPaint.setColor(getCurrentTextColor());
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.MONOSPACE);

        // Compute size of each box
        mMinBoxW = StyleUtils.getSize(res, R.dimen.puzzle_box_width);
        mTextPaint.setTextSize(StyleUtils.getSize(res, R.dimen.puzzle_hint_size));

        // Compute size of a single char (assumes monospaced font!)
        Rect bounds = new Rect();
        mTextPaint.getTextBounds("M", 0, 1, bounds);
        mCharW = bounds.width();
        mCharH = bounds.height();

        if (isInEditMode()) {
            setPuzzle(new Puzzle());
        }
    }

    @Nullable
    public Puzzle getPuzzle() {
        return mPuzzle;
    }

    public void setPuzzle(Puzzle puzzle) {
        mPuzzle = puzzle;
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

        // Pack the most number of characters in the bar
        int innerBox = width - getPaddingLeft();
        for (int i = 1; i < 26; i++) {
            mCharsPerRow = (int) Math.ceil(26f / i);
            mBoxW = innerBox / mCharsPerRow;
            if (mBoxW >= mMinBoxW) {
                break;
            }
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
        int desiredHeight = getPaddingTop();

        if (mPuzzle != null) {
            Collection<Character> userChars = mPuzzle.getUserChars();
            // Compute the height that works for this width
            float offsetY = mCharH / 2;
            float offsetX = (mBoxW / 2) - (mCharW / 2);
            float x = getPaddingLeft();
            // First row
            float y = getPaddingTop() + mCharH;
            char c = 'A';
            for (int i = 0; i < 26; i++) {
                if (i % mCharsPerRow == 0) {
                    x = getPaddingLeft();
                    if (i > 0) {
                        // Another row
                        y += mCharH + offsetY;
                    }
                } else {
                    // Box width
                    x += mBoxW;
                }
                if (canvas != null) {
                    String chr = String.valueOf(c);
                    // Check if it's been mapped already
                    if (userChars.contains(c)) {
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

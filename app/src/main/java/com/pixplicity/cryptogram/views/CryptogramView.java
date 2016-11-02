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
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;

import java.util.HashMap;


public class CryptogramView extends TextView {

    private static final String TAG = CryptogramView.class.getSimpleName();

    public static final int INPUT_TYPE = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

    private Cryptogram mCryptogram;
    private char mSelectedCharacter;

    private float mBoxW, mBoxH, mCharW1, mCharW2;
    private Paint mPaint, mLinePaint1, mLinePaint2, mBoxPaint1, mBoxPaint2;
    private TextPaint mTextPaintInput, mTextPaintInputComplete, mTextPaintMapping;
    private int mBoxInset;

    private OnCryptogramProgressListener mOnCryptogramProgressListener;


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
        Resources r = context.getResources();

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);

        mLinePaint1 = new Paint(mPaint);
        mLinePaint1.setStrokeWidth(r.getDimensionPixelSize(R.dimen.puzzle_line_height));
        mLinePaint1.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint2 = new Paint(mLinePaint1);
        mLinePaint2.setAlpha(96);

        mBoxPaint1 = new Paint(mPaint);
        mBoxPaint1.setColor(ContextCompat.getColor(context, R.color.box_highlight));
        mBoxPaint1.setStrokeWidth(r.getDimensionPixelSize(R.dimen.box_highlight_stroke));
        mBoxPaint1.setStyle(Paint.Style.FILL);
        mBoxPaint2 = new Paint(mBoxPaint1);
        mBoxPaint2.setStyle(Paint.Style.STROKE);

        mBoxInset = r.getDimensionPixelSize(R.dimen.box_highlight_stroke) / 2;

        mTextPaintInput = new TextPaint(mPaint);
        mTextPaintInput.setTypeface(Typeface.MONOSPACE);

        mTextPaintMapping = new TextPaint(mTextPaintInput);

        // Compute size of each box
        mBoxW = r.getDimensionPixelSize(R.dimen.puzzle_box_width);
        mBoxH = r.getDimensionPixelSize(R.dimen.puzzle_box_height);
        mTextPaintInput.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_text_size));
        mTextPaintMapping.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_hint_size));

        mTextPaintInputComplete = new TextPaint(mTextPaintInput);
        mTextPaintInputComplete.setColor(ContextCompat.getColor(context, R.color.textComplete));

        // Compute size of a single char (assumes monospaced font!)
        Rect bounds = new Rect();
        mTextPaintInput.getTextBounds("M", 0, 1, bounds);
        mCharW1 = bounds.width();

        if (isInEditMode()) {
            setCryptogram(new Cryptogram());
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showSoftInput();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setShowSoftInputOnFocus(true);
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            showSoftInput();
        } else {
            hideSoftInput();
        }
    }

    public void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // Don't consume
                return false;
            case KeyEvent.KEYCODE_ENTER:
                // Clear selection
                setSelectedCharacter((char) 0);
                return true;
        }
        return onKeyPress((char) event.getUnicodeChar());
    }

    private boolean onKeyPress(char c) {
        if (mCryptogram != null) {
            if (setCharacterMapping(getSelectedCharacter(), c)) {
                // Answer filled in; clear the selection
                setSelectedCharacter((char) 0);
            } else {
                // Make a selection
                setSelectedCharacter(c);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getInputType() {
        return INPUT_TYPE;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = INPUT_TYPE;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT;
        return new BaseInputConnection(this, true) {
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                onKeyPress((char) 0);
                return false;
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                String input = text.toString().trim();
                if (input.length() > 0) {
                    onKeyPress(input.charAt(0));
                } else {
                    onKeyPress((char) 0);
                }
                finishComposingText();
                return true;
            }
        };
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    public Cryptogram getCryptogram() {
        return mCryptogram;
    }

    public void setCryptogram(Cryptogram cryptogram) {
        mCryptogram = cryptogram;
        requestLayout();
    }

    public boolean hasSelectedCharacter() {
        return mSelectedCharacter != 0;
    }

    public char getSelectedCharacter() {
        return mSelectedCharacter;
    }

    public boolean setSelectedCharacter(char c) {
        // Character does not occur in the mapping
        mSelectedCharacter = 0;
        if (mCryptogram.isInputChar(c)) {
            c = Character.toUpperCase(c);
            HashMap<Character, Character> charMapping = mCryptogram.getCharMapping();
            for (Character chrOrig : charMapping.keySet()) {
                Character chrMapped = charMapping.get(chrOrig);
                if (chrMapped == c) {
                    // Current selection is the input character
                    mSelectedCharacter = chrOrig;
                    break;
                }
            }
        }
        invalidate();
        return mSelectedCharacter != 0;
    }

    public boolean setCharacterMapping(char selectedChar, char userChar) {
        if (selectedChar != 0) {
            if (mCryptogram.isRevealed(selectedChar)) {
                // This character was already revealed; don't allow the user to alter it
                mCryptogram.setUserChar(selectedChar, selectedChar);
                return true;
            }
            boolean wasCompleted = mCryptogram.isCompleted();
            boolean progressChange = wasCompleted;
            if (mCryptogram.isInputChar(userChar)) {
                // Enter the user's mapping
                mCryptogram.setUserChar(selectedChar, Character.toUpperCase(userChar));
                if (mCryptogram.isCompleted()) {
                    if (!wasCompleted) {
                        progressChange = true;
                    }
                    hideSoftInput();
                }
            } else {
                // Clear it
                mCryptogram.setUserChar(selectedChar, (char) 0);
            }
            if (progressChange && mOnCryptogramProgressListener != null) {
                mOnCryptogramProgressListener.onCryptogramProgress(mCryptogram);
            }
            invalidate();
            return true;
        }
        return false;
    }

    public boolean revealCharacterMapping(char c) {
        mCryptogram.reveal(c);
        return setCharacterMapping(c, c);
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

        int desiredHeight = 0;
        int height;

        if (mCryptogram != null) {
            // Compute the height that works for this width
            float offsetY = mBoxH / 4;
            float x = 0, y = mBoxH;
            for (String word : mCryptogram.getWords()) {
                float w = word.length() * mBoxW;
                if (x + w > width) {
                    x = 0;
                    y += mBoxH * 2 + offsetY * 2;
                }
                for (int i = 0; i < word.length(); i++) {
                    // Box width
                    x += mBoxW;
                }
                // Trailing space
                x += mBoxW;
            }
            desiredHeight = (int) (y + mBoxH + offsetY * 2);
        }

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

        if (mCryptogram == null) {
            // Nothing to do
            return;
        }

        HashMap<Character, Character> charMapping;
        if (isInEditMode()) {
            charMapping = new HashMap<>();
        } else {
            charMapping = mCryptogram.getCharMapping();
        }

        boolean completed = false;
        if (!isInEditMode() && mCryptogram.isCompleted()) {
            completed = true;
        }
        TextPaint textPaintUser = completed ? mTextPaintInputComplete : mTextPaintInput;
        mTextPaintMapping.setAlpha(completed ? 96 : 255);
        Paint linePaint = completed ? mLinePaint2 : mLinePaint1;

        float offsetX1 = (mBoxW - mCharW1) / 4;
        float offsetX2 = (mBoxW - mCharW2) / 4;
        float offsetY = mBoxH / 4;
        float x = 0, y = mBoxH;
        for (String word : mCryptogram.getWords()) {
            float w = word.length() * mBoxW;
            if (x + w > canvas.getWidth()) {
                x = 0;
                y += mBoxH * 2 + offsetY * 2;
            }
            for (int i = 0; i < word.length(); i++) {
                char c = Character.toUpperCase(word.charAt(i));
                String chr;
                Character mappedChar = charMapping.get(c);
                if (mSelectedCharacter == c) {
                    // The user is inputting this character; highlight it
                    canvas.drawRect(x + mBoxInset, y - mBoxH + mBoxInset, x + mBoxW - mBoxInset, y + offsetY - mBoxInset, mBoxPaint1);
                    canvas.drawRect(x + mBoxInset, y - mBoxH + mBoxInset, x + mBoxW - mBoxInset, y + offsetY - mBoxInset, mBoxPaint2);
                    //canvas.drawRect(x, y - mBoxH, x + mBoxW, y + offsetY, mBoxPaint2);
                }
                if (mappedChar != null) {
                    chr = String.valueOf(mappedChar);
                    canvas.drawText(chr, x + offsetX2, y + mBoxH + offsetY, mTextPaintMapping);
                }
                if (mCryptogram.isRevealed(c)) {
                    // This box has already been revealed to the user
                    canvas.drawLine(x + offsetX1, y + offsetY, x + mBoxW - offsetX1, y + offsetY, mLinePaint2);
                } else if (mCryptogram.isInputChar(c)) {
                    // This is a box the user has to fill to complete the puzzle
                    canvas.drawLine(x + offsetX1, y + offsetY, x + mBoxW - offsetX1, y + offsetY, linePaint);
                    c = getUserInput(c);
                }
                if (c > 0) {
                    chr = String.valueOf(c);
                    canvas.drawText(chr, x + offsetX1, y, textPaintUser);
                }
                // Box width
                x += mBoxW;
            }
            // Trailing space
            x += mBoxW;
        }
    }

    private char getUserInput(char c) {
        if (isInEditMode()) {
            return 0;
        }
        Character input = mCryptogram.getUserChar(c);
        if (input == null) {
            return 0;
        }
        return input;
    }

    public void setOnCryptogramProgressListener(OnCryptogramProgressListener onCryptogramProgressListener) {
        mOnCryptogramProgressListener = onCryptogramProgressListener;
    }

    public interface OnCryptogramProgressListener {

        void onCryptogramProgress(Cryptogram cryptogram);

    }

}

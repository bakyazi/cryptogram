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
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;

import java.util.HashMap;


public class CryptogramView extends TextView {

    private Cryptogram mCryptogram;
    private String[] mWords;
    private HashMap<Character, Character> mUserChars;

    private float mBoxW, mBoxH, mCharW1, mCharW2;
    private Paint mPaint;
    private Paint mLinePaint;
    private TextPaint mTextPaint1, mTextPaint2;


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

        mLinePaint = new Paint(mPaint);
        mLinePaint.setStrokeWidth(r.getDimensionPixelSize(R.dimen.puzzle_line_height));
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint1 = new TextPaint(mPaint);
        mTextPaint1.setTypeface(Typeface.MONOSPACE);

        mTextPaint2 = new TextPaint(mTextPaint1);

        // Compute size of each box
        mBoxW = r.getDimensionPixelSize(R.dimen.puzzle_box_width);
        mBoxH = r.getDimensionPixelSize(R.dimen.puzzle_box_height);
        mTextPaint1.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_text_size));
        mTextPaint2.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_hint_size));

        // Compute size of a single char (assumes monospaced font!)
        Rect bounds = new Rect();
        mTextPaint1.getTextBounds("M", 0, 1, bounds);
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
        }
    }

    private void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.actionLabel = "";
        outAttrs.hintText = "";
        outAttrs.initialCapsMode = 0;
        outAttrs.initialSelEnd = outAttrs.initialSelStart = -1;
        outAttrs.label = "";
        outAttrs.imeOptions = EditorInfo.IME_ACTION_UNSPECIFIED | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        return new BaseInputConnection(CryptogramView.this, false) {
            @Override
            public boolean setComposingText(CharSequence text,
                                            int newCursorPosition) {
                return super.setComposingText(text, newCursorPosition);
            }


            @Override
            public boolean finishComposingText() {
                return super.finishComposingText();
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                onKeyPress(text);
                return super.commitText(text, newCursorPosition);
            }
        };
    }

    private void onKeyPress(CharSequence text) {
        Toast.makeText(getContext(), "onKeyPress: " + text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    public void setCryptogram(Cryptogram cryptogram) {
        mCryptogram = cryptogram;
        mWords = mCryptogram.getWords();
        mUserChars = new HashMap<>();

        for (String word : mWords) {
            for (int i = 0; i < word.length(); i++) {
                mUserChars.put(word.charAt(i), (char) 0);
            }
        }

        // FIXME remove
        mUserChars.put('a', 'a');
        mUserChars.put('e', 'e');

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCryptogram == null) {
            // Nothing to do
            return;
        }

        HashMap<Character, Character> charMapping = mCryptogram.getCharMapping();

        float offsetX1 = (mBoxW - mCharW1) / 4;
        float offsetX2 = (mBoxW - mCharW2) / 4;
        float offsetY = mBoxH / 4;
        float x = 0, y = mBoxH;
        for (String word : mWords) {
            float w = word.length() * mBoxW;
            if (x + w > canvas.getWidth()) {
                x = 0;
                y += mBoxH * 2 + offsetY * 2;
            }
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                String chr;
                Character mappedChar = charMapping.get(c);
                if (mappedChar != null) {
                    chr = String.valueOf(mappedChar);
                    canvas.drawText(chr, x + offsetX2, y + mBoxH + offsetY, mTextPaint2);
                }
                if (mCryptogram.isInputChar(c)) {
                    canvas.drawLine(x + offsetX1, y + offsetY, x + mBoxW - offsetX1, y + offsetY, mLinePaint);
                    c = getUserInput(c);
                }
                if (c > 0) {
                    chr = String.valueOf(c);
                    canvas.drawText(chr, x + offsetX1, y, mTextPaint1);
                }
                // Box width
                x += mBoxW;
            }
            // Trailing space
            x += mBoxW;
        }
    }

    private char getUserInput(char c) {
        Character input = mUserChars.get(c);
        if (input == null) {
            return 0;
        }
        return input;
    }

}

package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.PrefsUtils;

import java.util.ArrayList;
import java.util.HashMap;


public class CryptogramView extends android.support.v7.widget.AppCompatTextView {

    private static final String TAG = CryptogramView.class.getSimpleName();

    private static final String SOFT_HYPHEN = "\u00AD";
    private static final boolean ENABLE_HYPHENATION = false;
    private static final boolean ENABLE_TOUCH_SELECTION = true;

    @Nullable
    private Cryptogram mCryptogram;

    private char mSelectedCharacter, mSelectedCharacterLast;
    private boolean mHighlightMistakes;

    private float mBoxW, mBoxH, mCharW1;
    private float mBoxPadding;
    private float mLineHeight;
    private Paint mPaint, mLinePaint1, mLinePaint2, mBoxPaint1, mBoxPaint2;
    private TextPaint mTextPaintInput, mTextPaintInputComplete, mTextPaintMapping, mTextPaintMistake;
    private int mBoxInset;

    private boolean mDarkTheme;

    private OnCryptogramProgressListener mOnCryptogramProgressListener;
    private OnHighlightListener mOnHighlightListener;
    private char[][] mCharMap;


    public CryptogramView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CryptogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CryptogramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Resources r = context.getResources();

        if (!isInEditMode()) {
            mDarkTheme = PrefsUtils.getDarkTheme();
        }

        mPaint = new Paint();
        int colorText, colorHighlight, colorComplete, colorMistake;
        if (mDarkTheme) {
            colorText = R.color.colorDarkPuzzleText;
            colorHighlight = R.color.colorDarkPuzzleHighlight;
            colorComplete = R.color.colorDarkPuzzleComplete;
            colorMistake = R.color.colorDarkPuzzleMistake;
        } else {
            colorText = R.color.colorPuzzleText;
            colorHighlight = R.color.colorPuzzleHighlight;
            colorComplete = R.color.colorPuzzleComplete;
            colorMistake = R.color.colorPuzzleMistake;
        }

        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(context, colorText));
        mPaint.setAntiAlias(true);

        mLinePaint1 = new Paint(mPaint);
        mLinePaint1.setStrokeWidth(r.getDimensionPixelSize(R.dimen.puzzle_line_height));
        mLinePaint1.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint2 = new Paint(mLinePaint1);
        mLinePaint2.setAlpha(96);

        mBoxPaint1 = new Paint(mPaint);

        mBoxPaint1.setColor(ContextCompat.getColor(context, colorHighlight));
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
        mBoxPadding = mBoxH / 4;
        mLineHeight = mBoxH * 2 + mBoxPadding * 2;
        mTextPaintInput.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_text_size));
        mTextPaintMapping.setTextSize(r.getDimensionPixelSize(R.dimen.puzzle_hint_size));

        mTextPaintInputComplete = new TextPaint(mTextPaintInput);
        mTextPaintInputComplete.setColor(ContextCompat.getColor(context, colorComplete));

        mTextPaintMistake = new TextPaint(mTextPaintInput);
        mTextPaintMistake.setColor(ContextCompat.getColor(context, colorMistake));

        // Compute size of a single char (assumes monospaced font!)
        Rect bounds = new Rect();
        mTextPaintInput.getTextBounds("M", 0, 1, bounds);
        mCharW1 = bounds.width();

        if (isInEditMode()) {
            setCryptogram(new Cryptogram());
        }

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
        if (mCryptogram != null && !mCryptogram.isCompleted()) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
            }
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
                return super.onKeyUp(keyCode, event);
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                if (mCryptogram != null) {
                    ArrayList<Character> charMapping = mCryptogram.getCharacterList();
                    int index = 0;
                    if (mSelectedCharacter == 0) {
                        mSelectedCharacter = mSelectedCharacterLast;
                    }
                    if (mSelectedCharacter != 0) {
                        index = charMapping.indexOf(mSelectedCharacter) + 1;
                    }
                    if (index >= charMapping.size()) {
                        index = 0;
                    }
                    if (charMapping.size() > index) {
                        char c = charMapping.get(index);
                        setSelectedCharacter(mCryptogram.getCharMapping().get(c));
                    } else {
                        setSelectedCharacter((char) 0);
                    }
                } else {
                    setSelectedCharacter((char) 0);
                }
                return true;
        }
        if (onKeyPress((char) event.getUnicodeChar())) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onKeyPress(char c) {
        if (mCryptogram != null && !mCryptogram.isCompleted()) {
            if (setUserChar(getSelectedCharacter(), c)) {
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
        return SimpleInputConnection.INPUT_TYPE;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = SimpleInputConnection.INPUT_TYPE;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        return new SimpleInputConnection(this);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Nullable
    public Cryptogram getCryptogram() {
        return mCryptogram;
    }

    public void setCryptogram(@Nullable Cryptogram cryptogram) {
        mCryptogram = cryptogram;
        mSelectedCharacter = mSelectedCharacterLast = 0;
        requestLayout();
    }

    public boolean hasSelectedCharacter() {
        return mSelectedCharacter != 0;
    }

    public char getSelectedCharacter() {
        return mSelectedCharacter;
    }

    public boolean setSelectedCharacter(char c) {
        // Stop highlighting mistakes
        mHighlightMistakes = false;
        // Character does not occur in the mapping
        mSelectedCharacter = 0;
        if (mCryptogram != null && mCryptogram.isInputChar(c)) {
            c = Character.toUpperCase(c);
            HashMap<Character, Character> charMapping = mCryptogram.getCharMapping();
            for (Character chrOrig : charMapping.keySet()) {
                Character chrMapped = charMapping.get(chrOrig);
                if (chrMapped == c) {
                    // Current selection is the input character
                    mSelectedCharacter = chrOrig;
                    mSelectedCharacterLast = chrOrig;
                    break;
                }
            }
        }
        invalidate();
        return mSelectedCharacter != 0;
    }

    public boolean setUserChar(char selectedChar, char userChar) {
        // Stop highlighting mistakes
        mHighlightMistakes = false;
        // Map the currently selected character to what the user inputs
        if (selectedChar != 0 && mCryptogram != null) {
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
            if (mOnCryptogramProgressListener != null) {
                mOnCryptogramProgressListener.onCryptogramProgress(mCryptogram);
            }
            invalidate();
            return true;
        }
        return false;
    }

    public void revealCharacterMapping(char c) {
        if (mCryptogram != null) {
            mCryptogram.reveal(c);
        }
        if (setUserChar(c, c)) {
            // Answer revealed; clear the selection
            setSelectedCharacter((char) 0);
        }
    }

    public void revealMistakes() {
        if (mCryptogram == null) {
            return;
        }
        if (!mHighlightMistakes) {
            mCryptogram.revealedMistakes();
            mHighlightMistakes = true;
        }
        invalidate();
    }

    public void reset() {
        mSelectedCharacter = 0;
        invalidate();
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
            float offsetY = mBoxH / 4;
            float y = drawOrMeasure(width, null);
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

        drawOrMeasure(canvas.getWidth(), canvas);
    }

    private float drawOrMeasure(float width, @Nullable Canvas canvas) {
        if (mCryptogram == null) {
            return 0;
        }
        HashMap<Character, Character> charMapping;
        if (isInEditMode()) {
            charMapping = null;
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

        PointF hyphenHighlight = null;

        mCharMap = new char[(int) (width / mBoxW)][100];

        float offsetX1 = (mBoxW - mCharW1) / 4;
        float x = 0, y = mBoxH;
        for (String word : mCryptogram.getWords()) {
            String displayWord = word.replace(SOFT_HYPHEN, "");
            if (!ENABLE_HYPHENATION) {
                word = displayWord;
            }
            float w = displayWord.length() * mBoxW;
            if (x + w > width) {
                // Whole word would exceed boundary
                // Check if we can use a soft hyphen
                int index = word.lastIndexOf(SOFT_HYPHEN);
                boolean needsLineBreak = true;
                while (index > -1) {
                    Log.d(TAG, "soft hyphen at index " + index);
                    if (x + (index + 1) * mBoxW <= width) {
                        // It fits with a soft hyphen; draw this segment
                        if (hyphenHighlight == null && canvas != null) {
                            hyphenHighlight = new PointF(x + index * mBoxW - mBoxW / 2, y - mBoxH / 2);
                            if (mOnHighlightListener != null) {
                                mOnHighlightListener.onHighlight(PrefsUtils.TYPE_HIGHLIGHT_HYPHENATION, hyphenHighlight);
                            }
                        }
                        String wordSegment = word.substring(0, index).replace(SOFT_HYPHEN, "") + "-";
                        x = drawWord(canvas, charMapping, textPaintUser, linePaint, offsetX1, x, y, wordSegment);
                        // Remainder of the word
                        word = word.substring(index + 1);
                        Log.d(TAG, "soft hyphen: " + wordSegment + " // " + word);
                        // Reset the search
                        index = word.lastIndexOf(SOFT_HYPHEN);
                        // Manually add a line break since nothing else will fit
                        x = 0;
                        y += mLineHeight;
                        needsLineBreak = false;
                    } else {
                        // It doesn't fit; look for a previous soft hyphen
                        index = word.lastIndexOf(SOFT_HYPHEN, index - 1);
                    }
                    if (x + word.length() * mBoxW < width) {
                        // The entire remaining word fits
                        break;
                    }
                }
                word = word.replace(SOFT_HYPHEN, "");
                if (needsLineBreak) {
                    x = 0;
                    y += mLineHeight;
                }
            } else {
                // Whole word fits; draw it
                word = displayWord;
            }
            x = drawWord(canvas, charMapping, textPaintUser, linePaint, offsetX1, x, y, word);
            // Trailing space
            x += mBoxW;
        }
        return y;
    }

    private float drawWord(@Nullable Canvas canvas, HashMap<Character, Character> charMapping,
                           TextPaint textPaintUser, Paint linePaint, float offsetX,
                           float x, float y, String word) {
        if (canvas == null || mCryptogram == null) {
            return x + mBoxW * word.length();
        }
        for (int i = 0; i < word.length(); i++) {
            char c = Character.toUpperCase(word.charAt(i));
            String chr;
            Character mappedChar = charMapping == null ? null : charMapping.get(c);
            if (mSelectedCharacter == c) {
                // The user is inputting this character; highlight it
                canvas.drawRect(x + mBoxInset, y - mBoxH + mBoxInset, x + mBoxW - mBoxInset, y + mBoxPadding - mBoxInset, mBoxPaint1);
                canvas.drawRect(x + mBoxInset, y - mBoxH + mBoxInset, x + mBoxW - mBoxInset, y + mBoxPadding - mBoxInset, mBoxPaint2);
                //canvas.drawRect(x, y - mBoxH, x + mBoxW, y + mBoxPadding, mBoxPaint2);
            }
            if (mappedChar != null) {
                chr = String.valueOf(mappedChar);
                canvas.drawText(chr, x + mBoxPadding, y + mBoxH + mBoxPadding, mTextPaintMapping);
                int xPos = (int) (x / mBoxW);
                int yPos = (int) (y / mLineHeight);
                mCharMap[yPos][xPos] = mappedChar;
            }
            if (mCryptogram.isRevealed(c)) {
                // This box has already been revealed to the user
                canvas.drawLine(x + offsetX, y + mBoxPadding, x + mBoxW - offsetX, y + mBoxPadding, mLinePaint2);
            } else if (mCryptogram.isInputChar(c)) {
                // This is a box the user has to fill to complete the puzzle
                canvas.drawLine(x + offsetX, y + mBoxPadding, x + mBoxW - offsetX, y + mBoxPadding, linePaint);
                c = getUserInput(c);
            }
            if (c > 0) {
                TextPaint textPaint = textPaintUser;
                if (mHighlightMistakes) {
                    Character correctMapping = mCryptogram.getCharacterForMapping(c);
                    if (mappedChar != correctMapping) {
                        textPaint = mTextPaintMistake;
                    }
                }
                // The character should be drawn in place
                chr = String.valueOf(c);
                canvas.drawText(chr, x + offsetX, y, textPaint);
            }
            // Box width
            x += mBoxW;
        }
        return x;
    }

    private String toString(char[][] array) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : array) {
            for (char c : row) {
                if (c == 0) {
                    sb.append(' ');
                } else {
                    sb.append(c);
                }
                sb.append(' ');
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (ENABLE_TOUCH_SELECTION) {
                    int y = (int) ((event.getY() - mBoxPadding) / mLineHeight);
                    int x = (int) ((event.getX() - mBoxPadding) / mBoxW);
                    char selected = 0;
                    if (y >= 0 && y < mCharMap.length) {
                        if (x >= 0 && x < mCharMap[y].length) {
                            selected = mCharMap[y][x];
                        }
                    }
                    setSelectedCharacter(selected);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private char getUserInput(char c) {
        if (mCryptogram != null) {
            Character input = mCryptogram.getUserChar(c);
            if (input != null) {
                return input;
            }
        }
        return 0;
    }

    public void setOnCryptogramProgressListener(
            OnCryptogramProgressListener onCryptogramProgressListener) {
        mOnCryptogramProgressListener = onCryptogramProgressListener;
    }

    public void setOnHighlightListener(OnHighlightListener onHighlightListener) {
        mOnHighlightListener = onHighlightListener;
    }

    public interface OnCryptogramProgressListener {

        void onCryptogramProgress(Cryptogram cryptogram);

    }

    public interface OnHighlightListener {

        void onHighlight(int type, PointF point);

    }

}

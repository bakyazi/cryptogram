package com.pixplicity.cryptogram.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.StyleUtils;

import java.util.ArrayList;
import java.util.HashMap;


public class CryptogramView extends AppCompatTextView {

    private static final String TAG = CryptogramView.class.getSimpleName();

    private static final String SOFT_HYPHEN = "\u00AD";
    public static final boolean ENABLE_HYPHENATION = false;

    private static final int KEYBOARD_ANIMATION_DURATION_MS = 200;

    @Nullable
    private Puzzle mPuzzle;

    private char mSelectedCharacter, mSelectedCharacterLast, mSelectedCharacterBeforeTouch;
    private boolean mHighlightMistakes;

    private float mBoxW, mBoxH, mCharW1;
    private float mBoxPadding;
    private float mLineHeight;
    private Paint mLinePaint1;
    private Paint mLinePaint2;
    private Paint mBoxPaint1;
    private Paint mBoxPaint2;
    private TextPaint mTextPaintInput, mTextPaintInputComplete, mTextPaintMapping, mTextPaintMistake;
    private int mBoxInset;

    private boolean mDarkTheme;

    private OnHighlightListener mOnHighlightListener;
    private char[][] mCharMap;
    private View mKeyboardView;


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
        Resources res = getResources();

        if (!isInEditMode()) {
            mDarkTheme = PrefsUtils.getDarkTheme();
        } else {
            mPuzzle = new Puzzle.Mock("This is an example puzzle.", "Author", "Topic");
        }

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

        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, colorText));
        paint.setAntiAlias(true);

        mLinePaint1 = new Paint(paint);
        mLinePaint1.setStrokeWidth(res.getDimensionPixelSize(R.dimen.puzzle_line_height));
        mLinePaint1.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint2 = new Paint(mLinePaint1);
        mLinePaint2.setAlpha(96);

        mBoxPaint1 = new Paint(paint);

        mBoxPaint1.setColor(ContextCompat.getColor(context, colorHighlight));
        mBoxPaint1.setStrokeWidth(res.getDimensionPixelSize(R.dimen.box_highlight_stroke));
        mBoxPaint1.setStyle(Paint.Style.FILL);
        mBoxPaint2 = new Paint(mBoxPaint1);
        mBoxPaint2.setStyle(Paint.Style.STROKE);

        mBoxInset = res.getDimensionPixelSize(R.dimen.box_highlight_stroke) / 2;

        mTextPaintInput = new TextPaint(paint);
        mTextPaintInput.setTypeface(Typeface.MONOSPACE);

        mTextPaintMapping = new TextPaint(mTextPaintInput);

        // Compute size of each box
        mBoxW = StyleUtils.getSize(res, R.dimen.puzzle_box_width);
        mBoxH = StyleUtils.getSize(res, R.dimen.puzzle_box_height);
        mBoxPadding = mBoxH / 4;
        mLineHeight = mBoxH * 2 + mBoxPadding * 2;
        mTextPaintInput.setTextSize(StyleUtils.getSize(res, R.dimen.puzzle_text_size));
        mTextPaintMapping.setTextSize(StyleUtils.getSize(res, R.dimen.puzzle_hint_size));

        mTextPaintInputComplete = new TextPaint(mTextPaintInput);
        mTextPaintInputComplete.setColor(ContextCompat.getColor(context, colorComplete));

        mTextPaintMistake = new TextPaint(mTextPaintInput);
        mTextPaintMistake.setColor(ContextCompat.getColor(context, colorMistake));

        // Compute size of a single char (assumes monospaced font!)
        Rect bounds = new Rect();
        mTextPaintInput.getTextBounds("M", 0, 1, bounds);
        mCharW1 = bounds.width();

        if (!isInEditMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setShowSoftInputOnFocus(PrefsUtils.getUseSystemKeyboard());
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
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

    public void setKeyboardView(View keyboardView) {
        mKeyboardView = keyboardView;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        showFocus(focused);
    }

    private void showFocus(boolean focused) {
        if (focused) {
            showSoftInput();
        } else {
            hideSoftInput();
        }
    }

    public void showSoftInput() {
        if (mPuzzle != null && !mPuzzle.isCompleted()) {
            // Show keyboard
            if (mKeyboardView == null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
                }
            } else {
                // Show built-in keyboard
                mKeyboardView.setVisibility(View.VISIBLE);
                mKeyboardView.animate()
                             .translationY(0)
                             .alpha(1.0f)
                             .setDuration(KEYBOARD_ANIMATION_DURATION_MS)
                             .setListener(null);
            }
        } else {
            hideSoftInput();
        }
    }

    public void hideSoftInput() {
        if (mKeyboardView != null) {
            // Hide built-in keyboard
            mKeyboardView.animate()
                         .translationY(mKeyboardView.getHeight())
                         .alpha(0.0f)
                         .setDuration(KEYBOARD_ANIMATION_DURATION_MS)
                         .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationEnd(Animator animation) {
                                 mKeyboardView.setVisibility(View.GONE);
                             }
                         });
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
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
                selectNextCharacter();
                return true;
        }
        if (onKeyPress((char) event.getUnicodeChar())) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void selectNextCharacter() {
        if (mPuzzle != null) {
            ArrayList<Character> charMapping = mPuzzle.getCharacterList();
            int index = 0;
            if (mSelectedCharacter == 0) {
                mSelectedCharacter = mSelectedCharacterLast;
            }
            // Respect user preference to skipp filled cells
            boolean skipFilledCells = PrefsUtils.getSkipFilledCells();
            char fallbackHintChar = 0;
            if (mSelectedCharacter != 0) {
                index = charMapping.indexOf(mSelectedCharacter) + 1;
            }
            int initialIndex = index;
            while (true) {
                if (index >= charMapping.size()) {
                    index = 0;
                }
                if (charMapping.size() > index) {
                    char c = charMapping.get(index);
                    Character hintChar = mPuzzle.getCharMapping().get(c);
                    if (skipFilledCells) {
                        if (fallbackHintChar == 0) {
                            fallbackHintChar = hintChar;
                        }
                        char userChar = getUserInput(c);
                        if (userChar != 0) {
                            // Cell not empty; continue searching
                            index++;
                            if (initialIndex == index) {
                                // We came full circle, no empty cell found
                                break;
                            }
                            continue;
                        }
                        // Found an empty cell
                    }
                    fallbackHintChar = 0;
                    setSelectedCharacter(hintChar == null ? 0 : hintChar);
                } else {
                    setSelectedCharacter((char) 0);
                }
                break;
            }
            if (fallbackHintChar != 0) {
                setSelectedCharacter(fallbackHintChar);
            }
        } else {
            setSelectedCharacter((char) 0);
        }
    }

    public boolean onKeyPress(char c) {
        if (mPuzzle != null && !mPuzzle.isCompleted()) {
            if (setUserChar(getSelectedCharacter(), c)) {
                // User filled this cell
                if (mPuzzle.isInputChar(c) && PrefsUtils.getAutoAdvance()) {
                    // Automatically advance to the next character
                    selectNextCharacter();
                } else {
                    // Clear the selection
                    setSelectedCharacter((char) 0);
                }
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
        if (PrefsUtils.getUseSystemKeyboard()) {
            return SimpleInputConnection.INPUT_TYPE;
        } else {
            return SimpleInputConnection.INPUT_NONE;
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (PrefsUtils.getUseSystemKeyboard()) {
            outAttrs.inputType = SimpleInputConnection.INPUT_TYPE;
            if (SimpleInputConnection.hasFaultyIme(getContext())) {
                outAttrs.inputType |= SimpleInputConnection.INPUT_TYPE_FOR_FAULTY_IME;
            }
            outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                outAttrs.imeOptions |= EditorInfo.IME_FLAG_FORCE_ASCII;
            }
            if (SimpleInputConnection.DISABLE_PERSONALIZED_LEARNING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING;
            }
            return new SimpleInputConnection(this);
        }
        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return PrefsUtils.getUseSystemKeyboard();
    }

    @Nullable
    public Puzzle getPuzzle() {
        return mPuzzle;
    }

    public void setPuzzle(@Nullable Puzzle puzzle) {
        mPuzzle = puzzle;
        mSelectedCharacter = mSelectedCharacterLast = 0;
        showFocus(hasFocus());
        requestLayout();
    }

    public boolean hasSelectedCharacter() {
        return mSelectedCharacter != 0;
    }

    public char getSelectedCharacter() {
        return mSelectedCharacter;
    }

    public void setSelectedCharacter(char c) {
        if (mPuzzle == null || mPuzzle.isCompleted()) {
            mSelectedCharacter = 0;
            return;
        }
        // Stop highlighting mistakes
        mHighlightMistakes = false;
        // Character does not occur in the mapping
        mSelectedCharacter = 0;
        if (mPuzzle.isInputChar(c)) {
            c = Character.toUpperCase(c);
            HashMap<Character, Character> charMapping = mPuzzle.getCharMapping();
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
        redraw();
    }

    public boolean setUserChar(char selectedChar, char userChar) {
        // Stop highlighting mistakes
        mHighlightMistakes = false;
        // Map the currently selected character to what the user inputs
        if (selectedChar != 0 && mPuzzle != null) {
            if (mPuzzle.isRevealed(selectedChar)) {
                // This character was already revealed; don't allow the user to alter it
                if (mPuzzle.setUserChar(selectedChar, selectedChar)) {
                    // TODO show highlight
                }
                return true;
            }
            // Check for completion state
            mPuzzle.isCompleted();
            if (mPuzzle.isInputChar(userChar)) {
                // Enter the user's mapping
                mPuzzle.setUserChar(selectedChar, Character.toUpperCase(userChar));
                if (mPuzzle.isCompleted()) {
                    hideSoftInput();
                }
            } else {
                // Clear it
                mPuzzle.setUserChar(selectedChar, (char) 0);
            }
            EventProvider.postEvent(new PuzzleEvent.PuzzleProgressEvent(mPuzzle));
            redraw();
            return true;
        }
        return false;
    }

    public void revealCharacterMapping(char c) {
        if (mPuzzle != null) {
            mPuzzle.reveal(c);
        }
        if (setUserChar(c, c)) {
            // Answer revealed; clear the selection
            setSelectedCharacter((char) 0);
        }
    }

    public void revealMistakes() {
        if (mPuzzle == null) {
            return;
        }
        if (!mHighlightMistakes) {
            mPuzzle.revealedMistakes();
            mHighlightMistakes = true;
        }
        redraw();
    }

    public void reset() {
        mSelectedCharacter = 0;
        redraw();
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

        if (mPuzzle != null) {
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

        if (mPuzzle == null) {
            // Nothing to do
            return;
        }

        drawOrMeasure(canvas.getWidth(), canvas);
    }

    private float drawOrMeasure(float width, @Nullable Canvas canvas) {
        if (mPuzzle == null) {
            return 0;
        }
        HashMap<Character, Character> charMapping;
        charMapping = mPuzzle.getCharMapping();

        boolean completed = false;
        if (mPuzzle.isCompleted()) {
            completed = true;
        }
        TextPaint textPaintUser = completed ? mTextPaintInputComplete : mTextPaintInput;
        mTextPaintMapping.setAlpha(completed ? 96 : 255);
        Paint linePaint = completed ? mLinePaint2 : mLinePaint1;

        PointF highlightPosition = null;

        mCharMap = new char[(int) (width / mBoxW)][100];

        float offsetX1 = (mBoxW - mCharW1) / 4;
        float x = 0, y = mBoxH;
        for (String word : mPuzzle.getWords()) {
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
                        if (highlightPosition == null && canvas != null) {
                            highlightPosition = new PointF(x + index * mBoxW - mBoxW / 2, y - mBoxH / 2);
                            if (mOnHighlightListener != null) {
                                mOnHighlightListener.onHighlight(PrefsUtils.TYPE_HIGHLIGHT_HYPHENATION, highlightPosition);
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
            if (x > 0 && y > mBoxH * 8) {
                // Take a more centered word
                if (mOnHighlightListener != null) {
                    PointF point = new PointF(x + mBoxW - mBoxW / 2, y);
                    mOnHighlightListener.onHighlight(PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT,
                            point);
                }
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
        if (canvas == null || mPuzzle == null) {
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
                if (yPos >= 0 && yPos < mCharMap.length) {
                    if (xPos >= 0 && xPos < mCharMap[yPos].length) {
                        mCharMap[yPos][xPos] = mappedChar;
                    }
                }
            }
            if (mPuzzle.isRevealed(c)) {
                // This box has already been revealed to the user
                canvas.drawLine(x + offsetX, y + mBoxPadding, x + mBoxW - offsetX, y + mBoxPadding, mLinePaint2);
            } else if (mPuzzle.isInputChar(c)) {
                // This is a box the user has to fill to complete the puzzle
                canvas.drawLine(x + offsetX, y + mBoxPadding, x + mBoxW - offsetX, y + mBoxPadding, linePaint);
                c = getUserInput(c);
            }
            if (c > 0) {
                TextPaint textPaint = textPaintUser;
                if (mHighlightMistakes) {
                    Character correctMapping = mPuzzle.getCharacterForMapping(c);
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

    public void redraw() {
        // TODO allow for buffering the image and issue a redraw here
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showSoftInput();
                if (mPuzzle != null) {
                    Character characterForMapping = mPuzzle.getCharacterForMapping(mSelectedCharacter);
                    mSelectedCharacterBeforeTouch = characterForMapping == null
                            ? 0 : characterForMapping;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int y = (int) ((event.getY() - mBoxPadding) / mLineHeight);
                int x = (int) ((event.getX() - mBoxPadding) / mBoxW);
                char selected = 0;
                if (y >= 0 && y < mCharMap.length) {
                    if (x >= 0 && x < mCharMap[y].length) {
                        selected = mCharMap[y][x];
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE && selected == 0) {
                    // Skip drag events for unselected characters
                } else {
                    setSelectedCharacter(selected);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                setSelectedCharacter(mSelectedCharacterBeforeTouch);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private char getUserInput(char c) {
        if (mPuzzle != null) {
            Character input = mPuzzle.getUserChar(c);
            if (input != null) {
                return input;
            }
        }
        return 0;
    }

    public void setOnHighlightListener(OnHighlightListener onHighlightListener) {
        mOnHighlightListener = onHighlightListener;
    }

    public interface OnHighlightListener {

        void onHighlight(int type, PointF point);

    }

}

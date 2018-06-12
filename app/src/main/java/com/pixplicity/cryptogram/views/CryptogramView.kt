package com.pixplicity.cryptogram.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.text.TextPaint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.events.PuzzleEvent
import com.pixplicity.cryptogram.models.Puzzle
import com.pixplicity.cryptogram.utils.EventProvider
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.pixplicity.cryptogram.utils.StyleUtils
import java.util.*


class CryptogramView : AppCompatTextView {

    companion object {
        const val ENABLE_HYPHENATION = false
        private const val SOFT_HYPHEN = "\u00AD"
        private const val KEYBOARD_ANIMATION_DURATION_MS = 200
    }

    private var mPuzzle: Puzzle? = null

    private var mSelectedCharacter: Char = ' '
    private var mSelectedCharacterLast: Char = ' '
    private var mSelectedCharacterBeforeTouch: Char = ' '
    private var mHighlightMistakes: Boolean = false

    private var mBoxW: Float = 0.toFloat()
    private var mBoxH: Float = 0.toFloat()
    private var mCharW1: Float = 0.toFloat()
    private var mBoxPadding: Float = 0.toFloat()
    private var mLineHeight: Float = 0.toFloat()
    private var mLinePaint1: Paint? = null
    private var mLinePaint2: Paint? = null
    private var mBoxPaint1: Paint? = null
    private var mBoxPaint2: Paint? = null
    private var mTextPaintInput: TextPaint? = null
    private var mTextPaintInputComplete: TextPaint? = null
    private var mTextPaintMapping: TextPaint? = null
    private var mTextPaintMistake: TextPaint? = null
    private var mBoxInset: Int = 0

    private var mDarkTheme: Boolean = false

    private var mOnHighlightListener: OnHighlightListener? = null
    private var mCharMap: Array<CharArray>? = null
    private var mKeyboardView: View? = null

    var puzzle: Puzzle?
        get() = mPuzzle
        set(puzzle) {
            mPuzzle = puzzle
            mSelectedCharacterLast = 0.toChar()
            mSelectedCharacter = mSelectedCharacterLast
            showFocus(hasFocus())
            requestLayout()
        }

    // Character does not occur in the mapping
    // Stop highlighting mistakes
    // Proceed to highlight the associated character
    // Current selection is the input character
    var selectedCharacter: Char
        get() = mSelectedCharacter
        set(c) {
            var c = c
            if (mPuzzle == null || mPuzzle!!.isCompleted) {
                mSelectedCharacter = 0.toChar()
                return
            }
            mSelectedCharacter = 0.toChar()
            if (mPuzzle!!.isInputChar(c)) {
                mHighlightMistakes = false
                c = Character.toUpperCase(c)
                val charMapping = mPuzzle!!.charMapping
                for (chrOrig in charMapping.keys) {
                    val chrMapped = charMapping[chrOrig]
                    if (chrMapped == c) {
                        mSelectedCharacter = chrOrig
                        mSelectedCharacterLast = chrOrig
                        break
                    }
                }
            }
            redraw()
        }


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val res = resources

        if (!isInEditMode) {
            mDarkTheme = PrefsUtils.darkTheme
        } else {
            mPuzzle = Puzzle.Mock("This is an example puzzle.", "Author", "Topic")
        }

        val colorText: Int
        val colorHighlight: Int
        val colorComplete: Int
        val colorMistake: Int
        if (mDarkTheme) {
            colorText = R.color.colorDarkPuzzleText
            colorHighlight = R.color.colorDarkPuzzleHighlight
            colorComplete = R.color.colorDarkPuzzleComplete
            colorMistake = R.color.colorDarkPuzzleMistake
        } else {
            colorText = R.color.colorPuzzleText
            colorHighlight = R.color.colorPuzzleHighlight
            colorComplete = R.color.colorPuzzleComplete
            colorMistake = R.color.colorPuzzleMistake
        }

        val paint = Paint()
        paint.color = ContextCompat.getColor(context, colorText)
        paint.isAntiAlias = true

        mLinePaint1 = Paint(paint)
        mLinePaint1!!.strokeWidth = res.getDimensionPixelSize(R.dimen.puzzle_line_height).toFloat()
        mLinePaint1!!.strokeCap = Paint.Cap.ROUND
        mLinePaint2 = Paint(mLinePaint1)
        mLinePaint2!!.alpha = 96

        mBoxPaint1 = Paint(paint)

        mBoxPaint1!!.color = ContextCompat.getColor(context, colorHighlight)
        mBoxPaint1!!.strokeWidth = res.getDimensionPixelSize(R.dimen.box_highlight_stroke).toFloat()
        mBoxPaint1!!.style = Paint.Style.FILL
        mBoxPaint2 = Paint(mBoxPaint1)
        mBoxPaint2!!.style = Paint.Style.STROKE

        mBoxInset = res.getDimensionPixelSize(R.dimen.box_highlight_stroke) / 2

        mTextPaintInput = TextPaint(paint)
        mTextPaintInput!!.typeface = Typeface.MONOSPACE

        mTextPaintMapping = TextPaint(mTextPaintInput)

        // Compute size of each box
        mBoxW = StyleUtils.getSize(res, R.dimen.puzzle_box_width).toFloat()
        mBoxH = StyleUtils.getSize(res, R.dimen.puzzle_box_height).toFloat()
        mBoxPadding = mBoxH / 4
        mLineHeight = mBoxH * 2 + mBoxPadding * 2
        mTextPaintInput!!.textSize = StyleUtils.getSize(res, R.dimen.puzzle_text_size).toFloat()
        mTextPaintMapping!!.textSize = StyleUtils.getSize(res, R.dimen.puzzle_hint_size).toFloat()

        mTextPaintInputComplete = TextPaint(mTextPaintInput)
        mTextPaintInputComplete!!.color = ContextCompat.getColor(context, colorComplete)

        mTextPaintMistake = TextPaint(mTextPaintInput)
        mTextPaintMistake!!.color = ContextCompat.getColor(context, colorMistake)

        // Compute size of a single char (assumes monospaced font!)
        val bounds = Rect()
        mTextPaintInput!!.getTextBounds("M", 0, 1, bounds)
        mCharW1 = bounds.width().toFloat()

        if (!isInEditMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showSoftInputOnFocus = PrefsUtils.useSystemKeyboard
        }
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventProvider.bus.register(this)
    }

    override fun onDetachedFromWindow() {
        EventProvider.bus.unregister(this)
        super.onDetachedFromWindow()
    }

    fun setKeyboardView(keyboardView: View?) {
        mKeyboardView = keyboardView
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        showFocus(focused)
    }

    private fun showFocus(focused: Boolean) {
        if (focused) {
            showSoftInput()
        } else {
            hideSoftInput()
        }
    }

    private fun showSoftInput() {
        if (mPuzzle != null && !mPuzzle!!.isCompleted) {
            // Show keyboard
            if (mKeyboardView == null) {
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            } else {
                // Show built-in keyboard
                mKeyboardView!!.visibility = View.VISIBLE
                mKeyboardView!!.animate()
                        .translationY(0f)
                        .alpha(1.0f)
                        .setDuration(KEYBOARD_ANIMATION_DURATION_MS.toLong())
                        .setListener(null)
            }
        } else {
            hideSoftInput()
        }
    }

    fun hideSoftInput() {
        if (mKeyboardView != null) {
            // Hide built-in keyboard
            mKeyboardView!!.animate()
                    .translationY(mKeyboardView!!.height.toFloat())
                    .alpha(0.0f)
                    .setDuration(KEYBOARD_ANIMATION_DURATION_MS.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mKeyboardView!!.visibility = View.GONE
                        }
                    })
        }
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK ->
                // Don't consume
                return super.onKeyUp(keyCode, event)
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NAVIGATE_NEXT -> {
                selectNextCharacter()
                return true
            }
        }
        return if (onKeyPress(event.unicodeChar.toChar())) {
            true
        } else super.onKeyUp(keyCode, event)
    }

    private fun selectNextCharacter() {
        if (mPuzzle != null) {
            val charMapping = mPuzzle!!.characterList
            var index = 0
            if (mSelectedCharacter.toInt() == 0) {
                mSelectedCharacter = mSelectedCharacterLast
            }
            // Respect user preference to skipp filled cells
            val skipFilledCells = PrefsUtils.skipFilledCells
            var fallbackHintChar: Char = 0.toChar()
            if (mSelectedCharacter.toInt() != 0) {
                index = charMapping.indexOf(mSelectedCharacter) + 1
            }
            val initialIndex = index
            while (true) {
                if (index >= charMapping.size) {
                    index = 0
                }
                if (charMapping.size > index) {
                    val c = charMapping[index]
                    val hintChar = mPuzzle!!.charMapping[c]
                    if (skipFilledCells) {
                        if (fallbackHintChar.toInt() == 0) {
                            fallbackHintChar = hintChar!!
                        }
                        val userChar = getUserInput(c)
                        if (userChar.toInt() != 0) {
                            // Cell not empty; continue searching
                            index++
                            if (initialIndex == index) {
                                // We came full circle, no empty cell found
                                break
                            }
                            continue
                        }
                        // Found an empty cell
                    }
                    fallbackHintChar = 0.toChar()
                    selectedCharacter = hintChar ?: 0.toChar()
                } else {
                    selectedCharacter = 0.toChar()
                }
                break
            }
            if (fallbackHintChar.toInt() != 0) {
                selectedCharacter = fallbackHintChar
            }
        } else {
            selectedCharacter = 0.toChar()
        }
    }

    fun onKeyPress(c: Char): Boolean {
        if (mPuzzle != null && !mPuzzle!!.isCompleted) {
            if (setUserChar(selectedCharacter, c)) {
                // User filled this cell
                if (mPuzzle!!.isInputChar(c) && PrefsUtils.autoAdvance) {
                    // Automatically advance to the next character
                    selectNextCharacter()
                } else {
                    // Clear the selection
                    selectedCharacter = 0.toChar()
                }
            } else {
                // Make a selection
                selectedCharacter = c
            }
            return true
        }
        return false
    }

    override fun getInputType(): Int {
        return if (PrefsUtils.useSystemKeyboard) {
            SimpleInputConnection.INPUT_TYPE
        } else {
            SimpleInputConnection.INPUT_NONE
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        if (PrefsUtils.useSystemKeyboard) {
            outAttrs.inputType = SimpleInputConnection.INPUT_TYPE
            if (SimpleInputConnection.hasFaultyIme(context)) {
                outAttrs.inputType = outAttrs.inputType or SimpleInputConnection.INPUT_TYPE_FOR_FAULTY_IME
            }
            outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                outAttrs.imeOptions = outAttrs.imeOptions or EditorInfo.IME_FLAG_FORCE_ASCII
            }
            if (SimpleInputConnection.DISABLE_PERSONALIZED_LEARNING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                outAttrs.imeOptions = outAttrs.imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            }
            return SimpleInputConnection(this)
        }
        return super.onCreateInputConnection(outAttrs)
    }

    override fun onCheckIsTextEditor(): Boolean {
        return PrefsUtils.useSystemKeyboard
    }

    fun hasSelectedCharacter(): Boolean {
        return mSelectedCharacter.toInt() != 0
    }

    private fun setUserChar(selectedChar: Char, userChar: Char): Boolean {
        // Stop highlighting mistakes
        mHighlightMistakes = false
        // Map the currently selected character to what the user inputs
        if (selectedChar.toInt() != 0 && mPuzzle != null) {
            mPuzzle?.let {
                if (it.isRevealed(selectedChar)) {
                    // This character was already revealed; don't allow the user to alter it
                    if (it.setUserChar(selectedChar, selectedChar)) {
                        // TODO show highlight
                    }
                } else {
                    // Check for completion state
                    it.isCompleted
                    if (it.isInputChar(userChar)) {
                        // Enter the user's mapping
                        it.setUserChar(selectedChar, Character.toUpperCase(userChar))
                        if (it.isCompleted) {
                            hideSoftInput()
                        }
                    } else {
                        // Clear it
                        it.setUserChar(selectedChar, 0.toChar())
                    }
                    EventProvider.postEvent(PuzzleEvent.PuzzleProgressEvent(it))
                }
            }
            redraw()
            return true
        }
        return false
    }

    fun revealCharacterMapping(c: Char) {
        mPuzzle?.reveal(c)
        if (setUserChar(c, c)) {
            // Answer revealed; clear the selection
            selectedCharacter = 0.toChar()
        }
        mPuzzle?.let {
            EventProvider.postEvent(PuzzleEvent.PuzzleProgressEvent(it))
        }
    }

    fun revealMistakes(): Int {
        if (mPuzzle == null) {
            return -1
        }
        if (!mHighlightMistakes) {
            mPuzzle!!.revealedMistakes()
            mHighlightMistakes = true
        }
        redraw()
        return mPuzzle!!.mistakeCount
    }

    fun reset() {
        mSelectedCharacter = 0.toChar()
        redraw()
        EventProvider.postEvent(PuzzleEvent.PuzzleResetEvent(mPuzzle))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = suggestedMinimumWidth
        val width: Int

        //Measure Width
        width = when (widthMode) {
            View.MeasureSpec.EXACTLY -> //Must be this size
                widthSize
            View.MeasureSpec.AT_MOST -> //Can't be bigger than...
                Math.min(desiredWidth, widthSize)
            View.MeasureSpec.UNSPECIFIED -> //Be whatever you want
                desiredWidth
            else -> throw IllegalStateException("widthMode $widthMode")
        }

        var desiredHeight = 0
        val height: Int

        if (mPuzzle != null) {
            val offsetY = mBoxH / 4
            val y = drawOrMeasure(width.toFloat(), null)
            desiredHeight = (y + mBoxH + offsetY * 2).toInt()
        }

        //Measure Height
        height = when (heightMode) {
            View.MeasureSpec.EXACTLY -> //Must be this size
                heightSize
            View.MeasureSpec.AT_MOST -> //Can't be bigger than...
                Math.min(desiredHeight, heightSize)
            View.MeasureSpec.UNSPECIFIED -> //Be whatever you want
                desiredHeight
            else -> throw IllegalStateException("widthMode $widthMode")
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mPuzzle == null) {
            // Nothing to do
            return
        }

        drawOrMeasure(width.toFloat(), canvas)
    }

    private fun drawOrMeasure(width: Float, canvas: Canvas?): Float {
        if (mPuzzle == null) {
            return 0f
        }
        val charMapping: HashMap<Char, Char> = mPuzzle!!.charMapping

        var completed = false
        if (mPuzzle!!.isCompleted) {
            completed = true
        }
        val textPaintUser = if (completed) mTextPaintInputComplete else mTextPaintInput
        mTextPaintMapping!!.alpha = if (completed) 96 else 255
        val linePaint = if (completed) mLinePaint2 else mLinePaint1

        var highlightPosition: PointF? = null

        mCharMap = Array(100) { CharArray((width / mBoxW).toInt()) }

        val offsetX1 = (mBoxW - mCharW1) / 4
        var x = 0f
        var y = mBoxH
        for (origWord in mPuzzle!!.words) {
            if (origWord == null) continue
            val displayWord = origWord.replace(SOFT_HYPHEN, "")
            var word = if (!ENABLE_HYPHENATION) displayWord else origWord
            val w = displayWord.length * mBoxW
            if (x + w > width) {
                // Whole word would exceed boundary
                // Check if we can use a soft hyphen
                var index = word.lastIndexOf(SOFT_HYPHEN)
                var needsLineBreak = true
                while (index > -1) {
                    if (x + (index + 1) * mBoxW <= width) {
                        // It fits with a soft hyphen; draw this segment
                        if (highlightPosition == null && canvas != null) {
                            highlightPosition = PointF(x + index * mBoxW - mBoxW / 2, y - mBoxH / 2)
                            if (mOnHighlightListener != null) {
                                mOnHighlightListener!!.onHighlight(PrefsUtils.TYPE_HIGHLIGHT_HYPHENATION, highlightPosition)
                            }
                        }
                        val wordSegment = word.substring(0, index).replace(SOFT_HYPHEN, "") + "-"
                        x = drawWord(canvas, charMapping, textPaintUser, linePaint, offsetX1, x, y, wordSegment)
                        // Remainder of the word
                        word = word.substring(index + 1)
                        // Reset the search
                        index = word.lastIndexOf(SOFT_HYPHEN)
                        // Manually add a line break since nothing else will fit
                        x = 0f
                        y += mLineHeight
                        needsLineBreak = false
                    } else {
                        // It doesn't fit; look for a previous soft hyphen
                        index = word.lastIndexOf(SOFT_HYPHEN, index - 1)
                    }
                    if (x + word.length * mBoxW < width) {
                        // The entire remaining word fits
                        break
                    }
                }
                word = word.replace(SOFT_HYPHEN, "")
                if (needsLineBreak) {
                    x = 0f
                    y += mLineHeight
                }
            } else {
                // Whole word fits; draw it
                word = displayWord
            }
            if (x > 0 && y > mBoxH * 8) {
                // Take a more centered word
                if (mOnHighlightListener != null) {
                    val point = PointF(x + mBoxW - mBoxW / 2, y)
                    mOnHighlightListener!!.onHighlight(PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT,
                            point)
                }
            }
            x = drawWord(canvas, charMapping, textPaintUser, linePaint, offsetX1, x, y, word)
            // Trailing space
            x += mBoxW
        }
        return y
    }

    private fun drawWord(canvas: Canvas?, charMapping: HashMap<Char, Char>?,
                         textPaintUser: TextPaint?, linePaint: Paint?, offsetX: Float,
                         x: Float, y: Float, word: String): Float {
        var newX = x
        if (canvas == null || mPuzzle == null) {
            return newX + mBoxW * word.length
        }
        for (i in 0 until word.length) {
            var c = Character.toUpperCase(word[i])
            var chr: String
            val mappedChar = if (charMapping == null) null else charMapping[c]
            if (mSelectedCharacter == c) {
                // The user is inputting this character; highlight it
                canvas.drawRect(newX + mBoxInset, y - mBoxH + mBoxInset, newX + mBoxW - mBoxInset, y + mBoxPadding - mBoxInset, mBoxPaint1!!)
                canvas.drawRect(newX + mBoxInset, y - mBoxH + mBoxInset, newX + mBoxW - mBoxInset, y + mBoxPadding - mBoxInset, mBoxPaint2!!)
                //canvas.drawRect(x, y - mBoxH, x + mBoxW, y + mBoxPadding, mBoxPaint2);
            }
            if (mappedChar != null) {
                chr = mappedChar.toString()
                canvas.drawText(chr, newX + mBoxPadding, y + mBoxH + mBoxPadding, mTextPaintMapping!!)
                val xPos = (newX / mBoxW).toInt()
                val yPos = (y / mLineHeight).toInt()
                if (yPos >= 0 && yPos < mCharMap!!.size) {
                    if (xPos >= 0 && xPos < mCharMap!![yPos].size) {
                        mCharMap!![yPos][xPos] = mappedChar
                    }
                }
            }
            if (mPuzzle!!.isRevealed(c)) {
                // This box has already been revealed to the user
                canvas.drawLine(newX + offsetX, y + mBoxPadding, newX + mBoxW - offsetX, y + mBoxPadding, mLinePaint2!!)
            } else if (mPuzzle!!.isInputChar(c)) {
                // This is a box the user has to fill to complete the puzzle
                canvas.drawLine(newX + offsetX, y + mBoxPadding, newX + mBoxW - offsetX, y + mBoxPadding, linePaint!!)
                c = getUserInput(c)
            }
            if (c.toInt() > 0) {
                var textPaint: TextPaint? = textPaintUser
                if (mHighlightMistakes) {
                    val correctMapping = mPuzzle!!.getCharacterForMapping(c)
                    if (mappedChar !== correctMapping) {
                        textPaint = mTextPaintMistake
                    }
                }
                // The character should be drawn in place
                chr = c.toString()
                canvas.drawText(chr, newX + offsetX, y, textPaint)
            }
            // Box width
            newX += mBoxW
        }
        return newX
    }

    fun redraw() {
        // TODO allow for buffering the image and issue a redraw here
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                showSoftInput()
                if (mPuzzle != null) {
                    val characterForMapping = mPuzzle!!.getCharacterForMapping(mSelectedCharacter)
                    mSelectedCharacterBeforeTouch = characterForMapping ?: 0.toChar()
                }
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                val y = ((event.y - mBoxPadding) / mLineHeight).toInt()
                val x = ((event.x - mBoxPadding) / mBoxW).toInt()
                var selected: Char = 0.toChar()
                if (y >= 0 && y < mCharMap!!.size) {
                    if (x >= 0 && x < mCharMap!![y].size) {
                        selected = mCharMap!![y][x]
                    }
                }
                if (event.action == MotionEvent.ACTION_MOVE && selected.toInt() == 0) {
                    // Skip drag events for unselected characters
                } else {
                    selectedCharacter = selected
                }
                if (event.action == MotionEvent.ACTION_UP) {
                    performClick()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                selectedCharacter = mSelectedCharacterBeforeTouch
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Confirmed simple tap
        return super.performClick()
    }

    private fun getUserInput(c: Char): Char {
        if (mPuzzle != null) {
            val input = mPuzzle!!.getUserChar(c)
            if (input != null) {
                return input
            }
        }
        return 0.toChar()
    }

    fun setOnHighlightListener(onHighlightListener: OnHighlightListener) {
        mOnHighlightListener = onHighlightListener
    }

    interface OnHighlightListener {

        fun onHighlight(type: Int, point: PointF)

    }

}

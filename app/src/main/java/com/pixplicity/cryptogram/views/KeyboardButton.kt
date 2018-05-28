package com.pixplicity.cryptogram.views

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.support.annotation.ColorInt
import android.support.v7.widget.AppCompatButton
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.events.PuzzleEvent
import com.pixplicity.cryptogram.models.Puzzle
import com.pixplicity.cryptogram.utils.EventProvider
import com.pixplicity.cryptogram.utils.KeyboardUtils
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.squareup.otto.Subscribe

class KeyboardButton : AppCompatButton, KeyboardUtils.Contract {

    private var mShowLetter: Boolean = false

    override var keyIndex: Int = 0

    private val mPath = Path()
    private val mPathPaint: Paint
    private val mTextPaint: TextPaint
    private val mTextColor: Int
    private val mTextColorGreyed: Int
    private val mViewBounds = Rect()
    private val mBox = Rect()
    private val mAlpha = 255

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // Get theme colors
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.keyboardBackground2, typedValue, true)
        @ColorInt val colorBg = typedValue.data
        theme.resolveAttribute(R.attr.keyboardForeground2, typedValue, true)
        @ColorInt val colorFg = typedValue.data

        mPathPaint = Paint()
        mPathPaint.color = colorBg
        mPathPaint.isAntiAlias = true
        mPathPaint.style = Paint.Style.FILL
        mTextPaint = TextPaint(mPathPaint)
        mTextPaint.color = colorFg
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.textSize = resources.getDimensionPixelSize(R.dimen.keyboard_popup_text_size).toFloat()
        mPathPaint.setShadowLayer(4.0f, 0.0f, 4.0f, Color.argb(200, 0, 0, 0))

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.KeyboardButton,
                defStyleAttr,
                R.style.KeyboardButton)

        try {
            keyIndex = a.getInteger(R.styleable.KeyboardButton_key, 0)
        } finally {
            a.recycle()
        }

        setOnClickListener { view -> KeyboardUtils.dispatch(this) }
        setOnTouchListener { view, motionEvent ->
            val action = motionEvent.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    // Draw letter press
                    mShowLetter = true
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    val show = isViewInBounds(motionEvent.x, motionEvent.y)
                    if (mShowLetter != show) {
                        mShowLetter = show
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isViewInBounds(motionEvent.x, motionEvent.y)) {
                        performClick()
                    }
                    mShowLetter = false
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                    mShowLetter = false
                    invalidate()
                }
            }
            true
        }
        text = KeyboardUtils.getKeyText(this)

        mTextColor = currentTextColor
        mTextColorGreyed = Color.argb(KeyboardUtils.Contract.ALPHA_GREYED, Color.red(mTextColor), Color.green(mTextColor), Color.blue(mTextColor))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventProvider.bus.register(this)
    }

    override fun onDetachedFromWindow() {
        EventProvider.bus.unregister(this)
        super.onDetachedFromWindow()
    }

    private fun isViewInBounds(x: Float, y: Float): Boolean {
        return mViewBounds.contains(Math.round(x), Math.round(y))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // Store view dimensions
        getDrawingRect(mViewBounds)

        // Store path for touch
        val parentWidth = (parent as View).width
        val width = right - left
        val boxPadding = resources.getDimensionPixelSize(R.dimen.keyboard_popup_padding)
        run {
            val boxWidth = resources.getDimensionPixelSize(R.dimen.keyboard_popup_width)
            val boxHeight = resources.getDimensionPixelSize(R.dimen.keyboard_popup_height)
            val boxLeft = Math.max(-left, Math.min(parentWidth - left - boxWidth, width / 2 - boxWidth / 2))
            if (keyIndex == 16 || keyIndex == 17) {
                Log.d(TAG, "onLayout: $keyIndex; $parentWidth - $right")
            }
            mBox.left = boxLeft
            mBox.top = -boxHeight
            mBox.right = boxLeft + boxWidth
            mBox.bottom = 0
        }
        val x1 = Math.max(mBox.left + boxPadding, boxPadding)
        val x2 = Math.min(mBox.right - boxPadding, width - boxPadding)
        // Inset below box
        mPath.moveTo(x1.toFloat(), boxPadding.toFloat())
        mPath.lineTo(x1.toFloat(), 0f)
        run {
            // Box itself
            // TODO mPath.quadTo() would be nicer
            mPath.lineTo(mBox.left.toFloat(), mBox.bottom.toFloat())
            mPath.lineTo(mBox.left.toFloat(), mBox.top.toFloat())
            mPath.lineTo(mBox.right.toFloat(), mBox.top.toFloat())
            mPath.lineTo(mBox.right.toFloat(), mBox.bottom.toFloat())
        }
        // Return to inset below box
        mPath.lineTo(x2.toFloat(), 0f)
        mPath.lineTo(x2.toFloat(), boxPadding.toFloat())
        mPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mShowLetter) {
            mTextPaint.alpha = mAlpha
            canvas.drawPath(mPath, mPathPaint)
            val x = mBox.left + (mBox.right - mBox.left) / 2
            val y = mBox.top + ((mBox.bottom - mBox.top) / 2 - (mTextPaint.descent() + mTextPaint.ascent()) / 2).toInt()
            canvas.drawText(KeyboardUtils.getKeyText(this)!!, x.toFloat(), y.toFloat(), mTextPaint)
        }
    }

    @Subscribe
    fun onPuzzleProgress(event: PuzzleEvent.PuzzleProgressEvent) {
        var input = false
        if (PrefsUtils.getShowUsedChars()) {
            val keyText = KeyboardUtils.getKeyText(this)
            if (keyText != null && keyText.isNotEmpty()) {
                val puzzle = event.puzzle
                input = puzzle.isUserCharInput(keyText[0])
            }
        }
        setTextColor(if (input) mTextColorGreyed else mTextColor)
        background.alpha = if (input) KeyboardUtils.Contract.Companion.ALPHA_GREYED else 255
    }

    companion object {

        private val TAG = KeyboardButton::class.java.simpleName
    }

}

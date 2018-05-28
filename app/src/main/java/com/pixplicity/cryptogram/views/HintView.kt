package com.pixplicity.cryptogram.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.support.v7.widget.AppCompatTextView
import android.text.TextPaint
import android.util.AttributeSet

import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.events.PuzzleEvent
import com.pixplicity.cryptogram.models.Puzzle
import com.pixplicity.cryptogram.utils.EventProvider
import com.pixplicity.cryptogram.utils.StyleUtils
import com.squareup.otto.Subscribe


class HintView : AppCompatTextView {

    private var mPuzzle: Puzzle? = null

    private var mCharsPerRow = 1

    private var mMinBoxW: Float = 0.toFloat()
    private var mBoxW: Float = 0.toFloat()
    private var mCharH: Float = 0.toFloat()
    private var mCharW: Float = 0.toFloat()
    private var mTextPaint: TextPaint? = null


    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val res = context.resources

        mTextPaint = TextPaint()
        mTextPaint!!.color = currentTextColor
        mTextPaint!!.isAntiAlias = true
        mTextPaint!!.typeface = Typeface.MONOSPACE

        // Compute size of each box
        mMinBoxW = StyleUtils.getSize(res, R.dimen.puzzle_box_width).toFloat()
        mTextPaint!!.textSize = StyleUtils.getSize(res, R.dimen.puzzle_hint_size).toFloat()

        // Compute size of a single char (assumes monospaced font!)
        val bounds = Rect()
        mTextPaint!!.getTextBounds("M", 0, 1, bounds)
        mCharW = bounds.width().toFloat()
        mCharH = bounds.height().toFloat()

        if (isInEditMode) {
            mPuzzle = Puzzle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventProvider.bus.register(this)
    }

    override fun onDetachedFromWindow() {
        EventProvider.bus.unregister(this)
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = suggestedMinimumWidth

        val width: Int
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize)
        } else {
            //Be whatever you want
            width = desiredWidth
        }

        // Pack the most number of characters in the bar
        val innerBox = width - paddingLeft
        for (i in 1..25) {
            mCharsPerRow = Math.ceil((26f / i).toDouble()).toInt()
            mBoxW = (innerBox / mCharsPerRow).toFloat()
            if (mBoxW >= mMinBoxW) {
                break
            }
        }

        val desiredHeight = drawChars(null, width)

        val height: Int
        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize)
        } else {
            //Be whatever you want
            height = desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawChars(canvas, this.width - paddingRight)
    }

    private fun drawChars(canvas: Canvas?, width: Int): Int {
        var desiredHeight = paddingTop

        if (mPuzzle != null) {
            // Compute the height that works for this width
            val offsetY = mCharH / 2
            val offsetX = mBoxW / 2 - mCharW / 2
            var x = paddingLeft.toFloat()
            // First row
            var y = paddingTop + mCharH
            var c = 'A'
            for (i in 0..25) {
                if (i % mCharsPerRow == 0) {
                    x = paddingLeft.toFloat()
                    if (i > 0) {
                        // Another row
                        y += mCharH + offsetY
                    }
                } else {
                    // Box width
                    x += mBoxW
                }
                if (canvas != null) {
                    val chr = c.toString()
                    // Check if it's been mapped already
                    if (mPuzzle!!.isUserCharInput(c)) {
                        mTextPaint!!.alpha = 96
                    } else {
                        mTextPaint!!.alpha = 255
                    }
                    // Draw the character
                    canvas.drawText(chr, x + offsetX, y, mTextPaint!!)
                }
                c++
            }
            desiredHeight = y.toInt()
        }

        desiredHeight += paddingBottom
        return desiredHeight
    }

    @Subscribe
    fun onPuzzleProgress(event: PuzzleEvent.PuzzleProgressEvent) {
        mPuzzle = event.puzzle
        requestLayout()
    }

}

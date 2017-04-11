package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class CryptogramLayout extends LinearLayout {

    private CryptogramView mCryptogramView;
    private GestureDetectorCompat mGestureDetector;

    public CryptogramLayout(Context context) {
        super(context);
        init(context);
    }

    public CryptogramLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CryptogramLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setClickable(true);
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                if (mCryptogramView != null) {
                    mCryptogramView.showSoftInput();
                }
                return false;
            }
        });
    }

    public void setCrytogramView(CryptogramView view) {
        mCryptogramView = view;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

}

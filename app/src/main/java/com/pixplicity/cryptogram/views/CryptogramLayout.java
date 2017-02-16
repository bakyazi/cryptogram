package com.pixplicity.cryptogram.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class CryptogramLayout extends LinearLayout {

    private CryptogramView mCryptogramView;

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
    }

    public void setCrytogramView(CryptogramView view) {
        mCryptogramView = view;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mCryptogramView != null) {
                    mCryptogramView.showSoftInput();
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

}

package com.pixplicity.cryptogram.views;

import android.annotation.SuppressLint;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mCryptogramView != null) {
                    mCryptogramView.hideSoftInput();
                    // Clear selected character
                    mCryptogramView.setSelectedCharacter((char) 0);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setCryptogramView(CryptogramView view) {
        mCryptogramView = view;
    }

}

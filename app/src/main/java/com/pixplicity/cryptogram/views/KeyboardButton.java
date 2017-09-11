package com.pixplicity.cryptogram.views;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;


public class KeyboardButton extends AppCompatButton {

    public KeyboardButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public KeyboardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public KeyboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final View focusedView = ((Activity) context).getCurrentFocus();
                if (focusedView != null) {
                    final int keycode = getKeycode();
                    focusedView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                            keycode, 0));
                    focusedView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
                            keycode, 0));
                }
            }
        });
    }

    private int getKeycode() {
        final CharSequence text = getText();
        if (text != null) {
            switch (text.toString()) {
                case "A":
                    return KeyEvent.KEYCODE_A;
                case "B":
                    return KeyEvent.KEYCODE_B;
                case "C":
                    return KeyEvent.KEYCODE_C;
                case "D":
                    return KeyEvent.KEYCODE_D;
                case "E":
                    return KeyEvent.KEYCODE_E;
                case "F":
                    return KeyEvent.KEYCODE_F;
                case "G":
                    return KeyEvent.KEYCODE_G;
                case "H":
                    return KeyEvent.KEYCODE_H;
                case "I":
                    return KeyEvent.KEYCODE_I;
                case "J":
                    return KeyEvent.KEYCODE_J;
                case "K":
                    return KeyEvent.KEYCODE_K;
                case "L":
                    return KeyEvent.KEYCODE_L;
                case "M":
                    return KeyEvent.KEYCODE_M;
                case "N":
                    return KeyEvent.KEYCODE_N;
                case "O":
                    return KeyEvent.KEYCODE_O;
                case "P":
                    return KeyEvent.KEYCODE_P;
                case "Q":
                    return KeyEvent.KEYCODE_Q;
                case "R":
                    return KeyEvent.KEYCODE_R;
                case "S":
                    return KeyEvent.KEYCODE_S;
                case "T":
                    return KeyEvent.KEYCODE_T;
                case "U":
                    return KeyEvent.KEYCODE_U;
                case "V":
                    return KeyEvent.KEYCODE_V;
                case "W":
                    return KeyEvent.KEYCODE_W;
                case "X":
                    return KeyEvent.KEYCODE_X;
                case "Y":
                    return KeyEvent.KEYCODE_Y;
                case "Z":
                    return KeyEvent.KEYCODE_Z;
                case "<-":
                    return KeyEvent.KEYCODE_DEL;
                case "->":
                    return KeyEvent.KEYCODE_ENTER;
            }
        }
        return KeyEvent.KEYCODE_SPACE;
    }

}

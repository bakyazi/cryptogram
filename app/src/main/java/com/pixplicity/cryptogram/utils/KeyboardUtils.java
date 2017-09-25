package com.pixplicity.cryptogram.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;

public class KeyboardUtils {

    private enum KeyValue {
        BACKSPACE(com.pixplicity.cryptogram.R.drawable.ic_keyboard_backspace, KeyEvent.KEYCODE_DEL),
        TAB(com.pixplicity.cryptogram.R.drawable.ic_keyboard_tab, KeyEvent.KEYCODE_ENTER),
        A("A", KeyEvent.KEYCODE_A),
        B("B", KeyEvent.KEYCODE_B),
        C("C", KeyEvent.KEYCODE_C),
        D("D", KeyEvent.KEYCODE_D),
        E("E", KeyEvent.KEYCODE_E),
        F("F", KeyEvent.KEYCODE_F),
        G("G", KeyEvent.KEYCODE_G),
        H("H", KeyEvent.KEYCODE_H),
        I("I", KeyEvent.KEYCODE_I),
        J("J", KeyEvent.KEYCODE_J),
        K("K", KeyEvent.KEYCODE_K),
        L("L", KeyEvent.KEYCODE_L),
        M("M", KeyEvent.KEYCODE_M),
        N("N", KeyEvent.KEYCODE_N),
        O("O", KeyEvent.KEYCODE_O),
        P("P", KeyEvent.KEYCODE_P),
        Q("Q", KeyEvent.KEYCODE_Q),
        R("R", KeyEvent.KEYCODE_R),
        S("S", KeyEvent.KEYCODE_S),
        T("T", KeyEvent.KEYCODE_T),
        U("U", KeyEvent.KEYCODE_U),
        V("V", KeyEvent.KEYCODE_V),
        W("W", KeyEvent.KEYCODE_W),
        X("X", KeyEvent.KEYCODE_X),
        Y("Y", KeyEvent.KEYCODE_Y),
        Z("Z", KeyEvent.KEYCODE_Z),
        SPACE(" ", KeyEvent.KEYCODE_SPACE),;

        public final String text;
        public final int drawableRes;
        public final int keycode;

        KeyValue(String text, int keycode) {
            this.text = text;
            this.drawableRes = 0;
            this.keycode = keycode;
        }

        KeyValue(@DrawableRes int drawableRes, int keycode) {
            this.text = null;
            this.drawableRes = drawableRes;
            this.keycode = keycode;
        }

        @NonNull
        private static KeyValue fromKeycode(int keyIndex) {
            return KeyValue.values()[keyIndex];
        }
    }

    public static void dispatch(final Contract contract) {
        final View focusedView = ((Activity) contract.getContext()).getCurrentFocus();
        if (focusedView != null) {
            int keycode = getKeycode(contract);
            focusedView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                    keycode, 0));
            focusedView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
                    keycode, 0));
        }
    }

    private static int getKeycode(Contract contract) {
        return KeyValue.fromKeycode(contract.getKeyIndex()).keycode;
    }

    public static String getKeyText(Contract contract) {
        return KeyValue.fromKeycode(contract.getKeyIndex()).text;
    }

    @DrawableRes
    public static int getKeyIcon(Contract contract) {
        return KeyValue.fromKeycode(contract.getKeyIndex()).drawableRes;
    }

    public interface Contract {

        Context getContext();

        void setOnClickListener(View.OnClickListener onClickListener);

        int getKeyIndex();

    }

}

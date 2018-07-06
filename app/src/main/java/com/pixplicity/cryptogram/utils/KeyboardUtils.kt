package com.pixplicity.cryptogram.utils

import android.support.annotation.DrawableRes
import android.view.KeyEvent
import com.pixplicity.cryptogram.events.PuzzleEvent

object KeyboardUtils {

    interface Contract {
        companion object {
            const val ALPHA_GREYED = (256 * 0.4f).toInt()
        }
        val keyIndex: Int
    }

    private enum class KeyValue(
            @DrawableRes val drawableRes: Int,
            val text: String?,
            val keycode: Int) {

        BACKSPACE(com.pixplicity.cryptogram.R.drawable.ic_keyboard_backspace, null, KeyEvent.KEYCODE_DEL),
        TAB(com.pixplicity.cryptogram.R.drawable.ic_keyboard_tab, null, KeyEvent.KEYCODE_ENTER),
        A(0, "A", KeyEvent.KEYCODE_A),
        B(0, "B", KeyEvent.KEYCODE_B),
        C(0, "C", KeyEvent.KEYCODE_C),
        D(0, "D", KeyEvent.KEYCODE_D),
        E(0, "E", KeyEvent.KEYCODE_E),
        F(0, "F", KeyEvent.KEYCODE_F),
        G(0, "G", KeyEvent.KEYCODE_G),
        H(0, "H", KeyEvent.KEYCODE_H),
        I(0, "I", KeyEvent.KEYCODE_I),
        J(0, "J", KeyEvent.KEYCODE_J),
        K(0, "K", KeyEvent.KEYCODE_K),
        L(0, "L", KeyEvent.KEYCODE_L),
        M(0, "M", KeyEvent.KEYCODE_M),
        N(0, "N", KeyEvent.KEYCODE_N),
        O(0, "O", KeyEvent.KEYCODE_O),
        P(0, "P", KeyEvent.KEYCODE_P),
        Q(0, "Q", KeyEvent.KEYCODE_Q),
        R(0, "R", KeyEvent.KEYCODE_R),
        S(0, "S", KeyEvent.KEYCODE_S),
        T(0, "T", KeyEvent.KEYCODE_T),
        U(0, "U", KeyEvent.KEYCODE_U),
        V(0, "V", KeyEvent.KEYCODE_V),
        W(0, "W", KeyEvent.KEYCODE_W),
        X(0, "X", KeyEvent.KEYCODE_X),
        Y(0, "Y", KeyEvent.KEYCODE_Y),
        Z(0, "Z", KeyEvent.KEYCODE_Z),
        SPACE(0, " ", KeyEvent.KEYCODE_SPACE)
    }

    private fun fromKeycode(keyIndex: Int): KeyValue {
        return KeyValue.values()[keyIndex]
    }

    fun dispatch(contract: Contract) {
        EventProvider.postEvent(
                PuzzleEvent.KeyboardInputEvent(getKeycode(contract)))
    }

    private fun getKeycode(contract: Contract): Int {
        return fromKeycode(contract.keyIndex).keycode
    }

    fun getKeyText(contract: Contract): String? {
        return fromKeycode(contract.keyIndex).text
    }

    @DrawableRes
    fun getKeyIcon(contract: Contract): Int {
        return fromKeycode(contract.keyIndex).drawableRes
    }

}

package com.pixplicity.cryptogram.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.pixplicity.cryptogram.utils.KeyboardUtils;
import com.pixplicity.cryptogram.views.KeyboardButton;

public class KeyboardLayout extends TableLayout {

    private final SparseArray<KeyboardButton> mButtons = new SparseArray<>();

    public KeyboardLayout(Context context) {
        this(context, null);
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        for (int i = 0; i < getChildCount(); i++) {
            View row = getChildAt(i);
            if (!(row instanceof TableRow)) {
                throw new IllegalStateException("Expected a TableRow; encountered " + row.getClass().getName());
            }
            for (int j = 0; j < ((TableRow) row).getChildCount(); j++) {
                View col = ((TableRow) row).getChildAt(j);
                if (col instanceof KeyboardButton) {
                    KeyboardButton button = (KeyboardButton) col;
                    char key = KeyboardUtils.getKeyChar(button);
                    if (key != 0) {
                        mButtons.put(key, button);
                    }
                }
            }
        }
    }

}

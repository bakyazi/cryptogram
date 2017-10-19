package com.pixplicity.cryptogram.fragments;

import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.pixplicity.cryptogram.activities.BaseActivity;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    /**
     * Color matrix that flips the components (<code>-1.0f * c + 255 = 255 - c</code>)
     * and keeps the alpha intact.
     */
    private static final float[] NEGATIVE = {
            -1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0  // alpha
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    public boolean isDarkTheme() {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            return ((BaseActivity) activity).isDarkTheme();
        }
        return false;
    }

    protected void invert(ImageView imageView) {
        imageView.getDrawable().setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
    }

    protected void showSnackbar(String text) {
        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).showSnackbar(text);
        }
    }

}

package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pixplicity.cryptogram.R;

public class AprilSpecialEdition {

    private static long sShownAt;

    public static boolean doSpecialMagicSauce(Context context) {
        if (System.currentTimeMillis() - sShownAt > 2 * 60 * 1000) {
            sShownAt = System.currentTimeMillis();
            View customView = LayoutInflater.from(context).inflate(R.layout.dialog_april, null);
            View ivApril = customView.findViewById(R.id.iv_april);
            RotateAnimation anim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(2000);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            ivApril.startAnimation(anim);
            new MaterialDialog.Builder(context)
                    .customView(customView, false)
                    .cancelable(false)
                    .positiveText(R.string.april_dialog_done1)
                    .show();
            return true;
        }
        return false;
    }

}

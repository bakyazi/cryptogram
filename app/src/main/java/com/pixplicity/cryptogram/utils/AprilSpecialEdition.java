package com.pixplicity.cryptogram.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class AprilSpecialEdition {

    public static final boolean TEST = true && BuildConfig.DEBUG;

    private static Calendar sCalSched, sCalNow = new GregorianCalendar();
    private static long sShownAt;

    public static boolean doSpecialMagicSauce(Context context) {
        sCalNow.setTimeInMillis(System.currentTimeMillis());
        if (sCalSched == null) {
            sCalSched = new GregorianCalendar();
            sCalSched.set(2017, Calendar.APRIL, 1, 7, 0, 0);
            if (TEST) {
                sCalSched.setTimeInMillis(sCalNow.getTimeInMillis());
                sCalSched.add(Calendar.SECOND, 1);
            }
        }
        if (sCalSched.before(sCalNow)) {
            sCalSched.add(Calendar.HOUR, 13);
            if (sCalSched.after(sCalNow)) {
                // It's april first
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
            }
        } else {
            // It's before April first; schedule an alarm
            long alarmTimestamp = sCalSched.getTimeInMillis();
            if (TEST) {
                alarmTimestamp = sCalNow.getTimeInMillis();
            }
            Intent notificationIntent = NotificationPublisher.getIntent(context, NotificationPublisher.NOTIFICATION_APRIL_SPECIAL);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            am.set(AlarmManager.RTC, alarmTimestamp, pi);
        }
        return false;
    }

}

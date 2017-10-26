package com.pixplicity.cryptogram.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.PuzzleActivity;

public class NotificationPublisher extends BroadcastReceiver {

    public static final int NOTIFICATION_SEASON_SPECIAL = 2001;

    private static final String NOTIFICATION_ID = "notification_id";

    public static Intent getIntent(Context context, int notificationId) {
        Intent notificationIntent = null;
        switch (notificationId) {
            case NOTIFICATION_SEASON_SPECIAL:
                return PuzzleActivity.create(context);
        }
        return notificationIntent;
    }

    public static void clear(Context context, int notificationId) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(notificationId);
    }

    public static void notify(Context context, int notificationId) {
        String channelId = null;
        int channelNameResId = 0;
        switch (notificationId) {
            case NOTIFICATION_SEASON_SPECIAL:
                channelId = "specials";
                channelNameResId = R.string.notification_channel_specials;
                break;
        }

        if (channelId == null) {
            throw new NullPointerException("Channel ID not defined for notification " + notificationId);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setDefaults(0);
        builder.setAutoCancel(true);

        PendingIntent pi = null;
        Intent intent = getIntent(context, notificationId);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pi = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        switch (notificationId) {
            case NOTIFICATION_SEASON_SPECIAL:
                builder.setContentTitle(context.getString(R.string.notification_season_special_title));
                builder.setContentText(context.getString(R.string.notification_season_special_text));
                break;
        }
        builder.setContentIntent(pi);

        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_LOW;
            builder.setPriority(importance);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, context.getString(channelNameResId),
                        importance);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }

        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notificationId, notification);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notify(context, id);
    }

}

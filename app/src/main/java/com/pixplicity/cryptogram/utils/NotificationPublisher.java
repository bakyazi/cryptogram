package com.pixplicity.cryptogram.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.pixplicity.cryptogram.R;

public class NotificationPublisher extends BroadcastReceiver {

    /**
     * Previously used for April Fools'.
     */
    @Deprecated
    public static final int NOTIFICATION_APRIL_SPECIAL = 2001;

    private static final String NOTIFICATION_ID = "notification_id";

    public static Intent getIntent(Context context, int notificationType) {
        Intent notificationIntent = null;
        switch (notificationType) {
            // TODO
        }
        return notificationIntent;
    }

    public static void clear(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    public static void notify(Context context, int id) {
        // Some defaults
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setAutoCancel(true);

        PendingIntent pi = null;
        switch (id) {
            // TODO
        }

        builder.setContentIntent(pi);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notify(context, id);
    }

}

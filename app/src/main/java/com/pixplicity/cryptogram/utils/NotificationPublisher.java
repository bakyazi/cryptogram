package com.pixplicity.cryptogram.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.CryptogramActivity;

public class NotificationPublisher extends BroadcastReceiver {

    public static final int NOTIFICATION_APRIL_SPECIAL = 2001;

    private static final String NOTIFICATION_ID = "notification_id";

    public static Intent getIntent(Context context, int notificationType) {
        Intent notificationIntent = null;
        switch (notificationType) {
            case NOTIFICATION_APRIL_SPECIAL: {
                notificationIntent = new Intent(context, NotificationPublisher.class);
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, NOTIFICATION_APRIL_SPECIAL);
            }
            break;
        }
        return notificationIntent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);

        // Some defaults
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setAutoCancel(true);

        PendingIntent pi = null;
        switch (id) {
            case NOTIFICATION_APRIL_SPECIAL: {
                builder.setContentText("Take a look at the April special edition");
                Intent notificationIntent = new Intent(context, CryptogramActivity.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                pi = PendingIntent.getActivity(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            break;
        }

        builder.setContentIntent(pi);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

}

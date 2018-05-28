package com.pixplicity.cryptogram.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.pixplicity.cryptogram.R

class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(NOTIFICATION_ID, 0)
        notify(context, id)
    }

    companion object {

        /**
         * Previously used for April Fools'.
         */
        @Deprecated("")
        val NOTIFICATION_APRIL_SPECIAL = 2001

        private val NOTIFICATION_ID = "notification_id"

        fun getIntent(context: Context, notificationType: Int): Intent? {
            val notificationIntent: Intent? = null
            when (notificationType) {

            }// TODO
            return notificationIntent
        }

        fun clear(context: Context, notificationId: Int) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        fun notify(context: Context, id: Int) {
            // Some defaults
            val builder = NotificationCompat.Builder(context)
            builder.setContentTitle(context.getString(R.string.app_name))
            builder.setSmallIcon(R.drawable.ic_notification)
            builder.setAutoCancel(true)

            val pi: PendingIntent? = null
            when (id) {

            }// TODO

            builder.setContentIntent(pi)
            val notification = builder.build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(id, notification)
        }
    }

}

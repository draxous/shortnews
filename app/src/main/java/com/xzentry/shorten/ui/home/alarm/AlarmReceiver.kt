package com.xzentry.shorten.ui.home.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.xzentry.shorten.R
import com.xzentry.shorten.notification.ForegroundNotification
import com.xzentry.shorten.ui.home.HomeActivity


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.getBooleanExtra(SHOW_NOTIFICATION, false) == true) {
            showNotification(context, intent?.getStringExtra(REMINDER_MESSAGE))
        }
    }

    private fun showNotification(context: Context, reminderMessage: String?) {
        val requestCode = 0
        createNotificationChannel(context)
        val notificationIntent = Intent(context, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode, notificationIntent, 0
        )
        val notification: Notification =
            NotificationCompat.Builder(context, ForegroundNotification.CHANNEL_ID)
                .setContentTitle(context.getString(R.string.reminder_notification_title))
                .setContentText(reminderMessage ?: context.getString(R.string.reminder_notification_content))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(requestCode, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                ForegroundNotification.CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val SHOW_NOTIFICATION = "showNotification"
        const val REMINDER_MESSAGE = "reminderMessage"

    }
}
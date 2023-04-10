package com.xzentry.shorten.notification.service

import android.annotation.TargetApi
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.xzentry.shorten.R
import me.leolin.shortcutbadger.ShortcutBadger


class NotificationBadgeIntentService : IntentService("NotificationBadgeIntentService") {
    private var notificationId = 0
    private var mNotificationManager: NotificationManager? = null
    override fun onCreate() {
        super.onCreate()
        mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val badgeCount = intent.getIntExtra("badgeCount", 0)
            mNotificationManager!!.cancel(notificationId)
            notificationId++
            val builder: Notification.Builder =
                Notification.Builder(applicationContext)
                    .setContentTitle("")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupNotificationChannel()
                builder.setChannelId(NOTIFICATION_CHANNEL)
            }
            val notification = builder.build()
            ShortcutBadger.applyNotification(
                applicationContext,
                notification,
                badgeCount
            )
            mNotificationManager!!.notify(notificationId, notification)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun setupNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL, "ShortcutBadger Sample",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        mNotificationManager!!.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL = "com.xzentry.shorten.notification.service"
    }
}
package com.xzentry.shorten.notification

import com.onesignal.OSNotification
import com.onesignal.OneSignal
import com.xzentry.shorten.AppController


class ShortNewsNotificationReceivedHandler(private val application: AppController) :
    OneSignal.NotificationReceivedHandler {
    override fun notificationReceived(notification: OSNotification?) {
        val data = notification!!.payload.additionalData
        val customKey: String?

        //if (ShortcutBadger.isBadgeCounterSupported(application.applicationContext)) {
            /*application.applicationContext.startService(
                Intent(application.applicationContext, NotificationBadgeIntentService::class.java).putExtra(
                    "badgeCount",
                    1
                )
            )*/
       // }

        if (data != null) {
            customKey = data.optString("postId", "")
        }
    }
}
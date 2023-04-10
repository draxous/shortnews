package com.xzentry.shorten.notification.service

import com.onesignal.NotificationExtenderService
import com.onesignal.OSNotificationReceivedResult

//notification not receives in some devices
//https://documentation.onesignal.com/docs/notifications-show-successful-but-are-not-being-shown
class ShortNewsNotificationService: NotificationExtenderService() {
    override fun onNotificationProcessing(notification: OSNotificationReceivedResult?): Boolean {
        // Return true to stop the notification from displaying.
        return false
    }
}
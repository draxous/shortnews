package com.xzentry.shorten.notification

import com.onesignal.OSNotificationAction
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal
import com.xzentry.shorten.AppController
import com.xzentry.shorten.ui.home.homeIntent


class ShortNewsNotificationOpenedHandler(private val application: AppController) :
    OneSignal.NotificationOpenedHandler {
    private var postId: String? = null

    override fun notificationOpened(result: OSNotificationOpenResult) { // Get custom datas from notification
        val data = result.notification.payload.additionalData
        if (data != null) {
            postId = data.optString("postId", "")
        }
        // React to button pressed
        val actionType = result.action.type
        if (actionType == OSNotificationAction.ActionType.ActionTaken) {
            //ShortcutBadger.removeCount(application)
            /* Log.i(
                 "OneSignalExample",
                 "Button pressed with id: " + result.action.actionID
             )*/
        }
        // Launch new activity using Application object
        startApp(postId)
    }

    private fun startApp(postId: String?) {
        application.startActivity(application.homeIntent(postId))
    }
}
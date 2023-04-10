package com.xzentry.shorten.utils

import android.content.ContentResolver
import android.provider.Settings
import android.text.Html
import android.view.View
import com.google.android.material.snackbar.Snackbar

object CommonUtils {

    fun showSnackBar(view: View, message: String) {
        Snackbar.make(view, Html.fromHtml("<font color=\"#ffffff\">$message</font>"), Snackbar.LENGTH_LONG).show()
    }
    fun deviceId(contentResolver: ContentResolver): String {
        return Settings.Secure.getString(contentResolver,
            Settings.Secure.ANDROID_ID).toString()
    }
}
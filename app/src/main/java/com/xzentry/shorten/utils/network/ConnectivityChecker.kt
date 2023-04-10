package com.xzentry.shorten.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import javax.inject.Inject


class ConnectivityChecker @Inject constructor(private val context: Context) {

    fun isMobileNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.run {
            isConnected && type == TYPE_MOBILE
        } ?: false
    }

    fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.run {
            isConnected && type == TYPE_WIFI
        } ?: false
    }
}
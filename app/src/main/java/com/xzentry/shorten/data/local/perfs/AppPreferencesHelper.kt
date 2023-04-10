package com.xzentry.shorten.data.local.perfs

import android.content.Context
import android.content.SharedPreferences

class AppPreferencesHelper(context: Context) : PreferencesHelper {

    companion object {

        private const val PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN"
        private const val PREF_KEY_DEVICE_ID = "PREF_KEY_DEVICE_ID"
        private const val PREF_KEY_LATEST_POST_UPDATED_AT = "PREF_KEY_LATEST_POST_UPDATED_AT"
        private const val PREF_KEY_LAST_DELETED_DATE = "PREF_KEY_LAST_DELETED_DATE"
        private const val PREF_KEY_LAST_APP_OPENED_DATE = "PREF_KEY_LAST_APP_OPENED_DATE"
        private const val PREF_KEY_OLDEST_POST_UPDATED_AT = "PREF_KEY_OLDEST_POST_UPDATED_AT"
        private const val PREF_USER_ONBOARD = "PREF_USER_ONBOARD"
        private const val PREF_KEY_DEVICE_ID_REGISTERED = "PREF_KEY_DEVICE_ID_REGISTERED"
        private const val PREF_KEY_USER_TYPE = "PREF_KEY_USER_TYPE"
        private const val PREF_KEY_LAST_VIEWED_CARD_POSITION = "PREF_KEY_LAST_VIEWED_CARD_POSITION"
    }

    private var prefs: SharedPreferences =
        context.getSharedPreferences("news_pref", Context.MODE_PRIVATE)

    override fun getAccessToken(): String? {
        return prefs.getString(PREF_KEY_ACCESS_TOKEN, null)
    }

    override fun setAccessToken(accessToken: String) {
        prefs.edit().putString(PREF_KEY_ACCESS_TOKEN, accessToken).apply()
    }


    override fun getDeviceId(): String? {
        return prefs.getString(PREF_KEY_DEVICE_ID, "")
    }

    override fun setDeviceId(deviceId: String) {
        prefs.edit().putString(PREF_KEY_DEVICE_ID, deviceId).apply()
    }

    override fun getLatestNewsPostUpdatedAtTime(): String? {
        return prefs.getString(PREF_KEY_LATEST_POST_UPDATED_AT, "-1")
    }

    override fun setLatestNewsPostUpdatedAtTime(lastPostUpdatedAt: String) {
        prefs.edit().putString(PREF_KEY_LATEST_POST_UPDATED_AT, lastPostUpdatedAt).apply()
    }

    override fun getDeletedDate(): String? {
        return prefs.getString(PREF_KEY_LAST_DELETED_DATE, "-1")
    }

    override fun setDeletedDate(lastDeletedDate: String) {
        prefs.edit().putString(PREF_KEY_LAST_DELETED_DATE, lastDeletedDate).apply()
    }

    override fun getLastAppOpenedDate(): String? {
        return prefs.getString(PREF_KEY_LAST_APP_OPENED_DATE, "-1")
    }

    override fun setLastAppOpenedDate(lastAppOpenedTime: String) {
        prefs.edit().putString(PREF_KEY_LAST_APP_OPENED_DATE, lastAppOpenedTime).apply()
    }

    override fun getOldestLoadedNewsPostUpdatedAtTime(): String? {
        return prefs.getString(PREF_KEY_OLDEST_POST_UPDATED_AT, "-1")
    }

    override fun setOldestLoadedNewsPostUpdatedAtTime(oldestPostUpdatedAt: String) {
        prefs.edit().putString(PREF_KEY_OLDEST_POST_UPDATED_AT, oldestPostUpdatedAt).apply()
    }

    override fun isDeviceRegistered(): Boolean {
        return prefs.getBoolean(PREF_KEY_DEVICE_ID_REGISTERED, false)
    }

    override fun registerDevice(isDeviceIdRegistered: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_DEVICE_ID_REGISTERED, isDeviceIdRegistered).apply()
    }

    override fun isUserOnBoard(): Boolean {
        return prefs.getBoolean(PREF_USER_ONBOARD, false)
    }

    override fun regsiterUserOnBoard(isUserOnBoard: Boolean) {
        prefs.edit().putBoolean(PREF_USER_ONBOARD, isUserOnBoard).apply()
    }

    override fun setUserType(userType: Int) {
        prefs.edit().putInt(PREF_KEY_USER_TYPE, userType).apply()
    }

    override fun getUserType(): Int {
        return prefs.getInt(PREF_KEY_USER_TYPE, -1)
    }

    override fun setLastViewedCardPosition(position: Int) {
        prefs.edit().putInt(PREF_KEY_LAST_VIEWED_CARD_POSITION, position).apply()
    }

    override fun getLastViewedCardPosition(): Int {
        return prefs.getInt(PREF_KEY_LAST_VIEWED_CARD_POSITION, 0)
    }

}
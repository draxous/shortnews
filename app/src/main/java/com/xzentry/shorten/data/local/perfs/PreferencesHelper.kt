package com.xzentry.shorten.data.local.perfs

interface PreferencesHelper {
    fun getAccessToken(): String?

    fun setAccessToken(accessToken: String)

    fun getDeviceId(): String?

    fun setDeviceId(deviceId: String)

    fun getLatestNewsPostUpdatedAtTime(): String?

    fun setLatestNewsPostUpdatedAtTime(lastPostUpdatedAt: String)

    fun getDeletedDate(): String?

    fun setDeletedDate(lastPostUpdatedAt: String)

    fun getLastAppOpenedDate(): String?

    fun setLastAppOpenedDate(lastAppOpenedTime: String)

    fun getOldestLoadedNewsPostUpdatedAtTime(): String?

    fun setOldestLoadedNewsPostUpdatedAtTime(oldestPostUpdatedAt: String)

    fun isDeviceRegistered(): Boolean

    fun isUserOnBoard(): Boolean

    fun regsiterUserOnBoard(isUserOnBoard: Boolean)


    fun registerDevice(isDeviceIdRegistered: Boolean)
//-1 if not registerd
    fun setUserType(userType: Int)

    fun getUserType(): Int

    fun setLastViewedCardPosition(position: Int)

    fun getLastViewedCardPosition(): Int

}
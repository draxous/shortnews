package com.xzentry.shorten.utils

import com.xzentry.shorten.BuildConfig

fun String?.toSnImageUrl(firebaseHelper: FirebaseHelper): String {
    this?.let { imageUrl ->
        return (firebaseHelper.imageBaseUrlRemote
            ?: BuildConfig.BASE_IMAGE_URL) + imageUrl
    }
    return ""
}
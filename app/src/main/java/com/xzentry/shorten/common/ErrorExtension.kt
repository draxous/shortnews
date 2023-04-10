package com.xzentry.shorten.common

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.xzentry.shorten.BuildConfig

inline fun <reified T : Any> T.debug(message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        if (throwable != null) {
            Log.d(T::class.java.simpleName, message, throwable)
        } else {
            Log.d(T::class.java.simpleName, message)
        }
    }
}

inline fun <reified T : Any> T.info(message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        if (throwable != null) {
            Log.i(T::class.java.simpleName, message, throwable)
        } else {
            Log.i(T::class.java.simpleName, message)
        }
    }
}

inline fun <reified T : Any> T.warn(message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        if (throwable != null) {
            Log.w(T::class.java.simpleName, message, throwable)
        } else {
            Log.w(T::class.java.simpleName, message)
        }
    }
}

inline fun <reified T : Any> T.error(message: String, throwable: Throwable? = null) {
    when {
        BuildConfig.IS_CRASHLYTICS_ENABLED -> FirebaseCrashlytics.getInstance()
            .recordException(throwable ?: IllegalStateException(message))
        throwable != null -> Log.e(T::class.java.simpleName, message, throwable)
        else -> Log.e(T::class.java.simpleName, message)
    }
}

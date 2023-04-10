package com.xzentry.shorten.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

interface FirebaseHelper {
    var imageBaseUrlRemote: String?
    var firebaseVersionName : String // Newest application version name available on firebase
    var pinnedImageUrlRemote: String? // Change this url in fire base to change the pinned image
    var pinnedWebUrlRemote: String? // Change this url in fire base to change the pinned image
    var reminderAlert: String? // Change this url in fire base to change the pinned image
    var isNewVersionAvailable: Boolean
    var isPinnedCardAvail: Boolean
    var deepLinkType: Int
    fun init()
    fun initDeepLink(activity: AppCompatActivity, success: Consumer<String>? = null, failure: Consumer<String>? = null)
    fun remoteConfigUpdatesCheck(validVersion: Consumer<String>? = null, invalidVersion: Consumer<String>? = null, isPinnedCardAvailable: Consumer<Boolean>? = null)
    fun configGoogleSignIn(
        context: Context
    ):GoogleSignInClient

    fun getAuth(): FirebaseAuth
    fun getUser(): FirebaseUser?
}
package com.xzentry.shorten.utils

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.common.error
import org.json.JSONObject
import javax.inject.Inject

class FirebaseHelperImpl @Inject constructor(val context: Context) : FirebaseHelper {
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig
    private var firebaseBuildNumber = BuildConfig.VERSION_CODE
    override var imageBaseUrlRemote: String? = null
    override var pinnedImageUrlRemote: String? = null
    override var pinnedWebUrlRemote: String? = null
    override var reminderAlert: String? = null
    override var firebaseVersionName = BuildConfig.VERSION_NAME
    override var isNewVersionAvailable = false // Default value will be false
    override var isPinnedCardAvail = false // Default value will be false
    override var deepLinkType = 0


    override fun init() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.activate()
    }

    override fun initDeepLink(
        activity: AppCompatActivity, success: Consumer<String>?,
        failure: Consumer<String>?
    ) {
        Firebase.dynamicLinks
            .getDynamicLink(activity.intent)
            .addOnSuccessListener(activity) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                if (deepLink != null) {
                    deepLinkType = deepLink.getQueryParameter("type")?.toInt() ?: 0
                    success?.accept(null)
                } else {

                    error("no dynamic link found", null)
                    failure?.accept(null)
                }
            }
            .addOnFailureListener(activity) { e ->
                error("failure retrieving dynamic link", e)
                failure?.accept(null)
            }
    }


    override fun remoteConfigUpdatesCheck(
        validVersion: Consumer<String>?,
        invalidVersion: Consumer<String>?,
        isPinnedCardAvailable: Consumer<Boolean>?
    ) {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                val remoteConfigData =
                    firebaseRemoteConfig.getString(BuildConfig.FIREBASE_REMOTE_KEY)
                val jsonObject = JSONObject(remoteConfigData)
                if (jsonObject.has("image_base_url")) {
                    //firebaseBuildNumber = jsonObject.optString("build_number", firebaseBuildNumber.toString()).toInt()
                    imageBaseUrlRemote = jsonObject.optString("image_base_url", imageBaseUrlRemote)
                    firebaseVersionName =
                        jsonObject.optString("latest_version_name", firebaseVersionName)
                    firebaseBuildNumber = jsonObject.optInt("latest_build_number", 0)
                    pinnedImageUrlRemote =
                        jsonObject.optString("pinned_image_url", pinnedImageUrlRemote)
                    pinnedWebUrlRemote = jsonObject.optString("pinned_web_url", pinnedWebUrlRemote)
                    reminderAlert = jsonObject.optString("reminder_alert_message", reminderAlert)
                }

                isNewVersionAvailable = firebaseBuildNumber > BuildConfig.VERSION_CODE
                isPinnedCardAvail = !pinnedImageUrlRemote.isNullOrEmpty()
                when {
                    isNewVersionAvailable -> {
                        invalidVersion?.accept(null)
                    }
                    isPinnedCardAvail -> {
                        isPinnedCardAvailable?.accept(null)
                    }
                    else -> {
                        validVersion?.accept(null)
                    }
                }
            }
            .addOnFailureListener {
                //validVersion?.accept(null)
                error(it.localizedMessage)
            }
    }

    override fun configGoogleSignIn(
        context: Context
    ): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        return  GoogleSignIn.getClient(context, gso)
    }

    override fun getAuth(): FirebaseAuth {
        return Firebase.auth
    }
    override fun getUser(): FirebaseUser? {
        return getAuth().currentUser
    }
}
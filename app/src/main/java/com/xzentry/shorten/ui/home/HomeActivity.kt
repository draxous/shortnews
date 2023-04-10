package com.xzentry.shorten.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.databinding.ActivityHomeBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.model.CardType
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.home.alarm.AlarmReceiver
import com.xzentry.shorten.ui.news.NewsFragment
import com.xzentry.shorten.ui.news.NewsViewModel
import com.xzentry.shorten.ui.news.OnGoogleSignUpCompleteListener
import com.xzentry.shorten.ui.topics.TopicsFragment
import com.xzentry.shorten.ui.webview.WebViewFragment
import com.xzentry.shorten.utils.CommonUtils
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.toDateyyyyMMddhhmm
import com.xzentry.shorten.utils.toStringyyyyMMddhhmm
import dagger.android.AndroidInjection
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.util.*
import javax.inject.Inject


private const val EXTRA_NOTIFICATION_POST_ID = "EXTRA_NOTIFICATION_POST_ID"

fun Context.homeIntent(postId: String?): Intent {
    return Intent(this, HomeActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .putExtra(EXTRA_NOTIFICATION_POST_ID, postId)
}

class HomeActivity : AppCompatActivity(), HomeNavigator {

    private lateinit var googleSignInListener: OnGoogleSignUpCompleteListener

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPrefs: PreferencesHelper

    @Inject
    lateinit var firebaseHelper: FirebaseHelper


    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var binding: ActivityHomeBinding

    lateinit var homeViewModel: HomeViewModel

    private var updateWebViewListener: OnUpdateWebViewListener? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    interface OnUpdateWebViewListener {
        fun onUpdateWebView(selectedNews: NewsData)
        fun onFooterClicked(selectedNews: NewsData)
    }

    fun setUpdateWebViewListener(listener: OnUpdateWebViewListener) {
        updateWebViewListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        //setting up last app opened date.
        val date = Date()
        sharedPrefs.setLastAppOpenedDate(date.toStringyyyyMMddhhmm().toString())

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.indicatorVisible = true
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        homeViewModel.navigator = this
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setViewPager(intent)
        initDeepLink()
        configGoogleSignIn()
        reminderNotificationWhenNotUsedRecently()
    }

    private fun configGoogleSignIn() {
        googleSignInClient = firebaseHelper.configGoogleSignIn(this)
        auth = firebaseHelper.getAuth()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "signin failed" + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setOnGoogleSignUpCompleteListener(callback : OnGoogleSignUpCompleteListener){
        googleSignInListener = callback
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val newsFragmentViewModel =
            ViewModelProvider(this, viewModelFactory).get(NewsViewModel::class.java)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    googleSignInListener.onGoogleSignUpSuccess()
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    googleSignInListener.onGoogleSignUpFail()
                    // updateUI(null)
                }
            }
    }

    private fun initDeepLink() {
        val success: Consumer<String> = Consumer {
            when (firebaseHelper.deepLinkType) {
                1 -> {
                    firebaseEventLog()
                }
                else -> {
                }
            }
        }

        val failure: Consumer<String> = Consumer {

        }

        firebaseHelper.initDeepLink(this@HomeActivity, success, failure)
    }

    override fun onResume() {
        super.onResume()

        //register device if it's not registered already
        if (!sharedPrefs.isDeviceRegistered() && !BuildConfig.DEBUG) {
            homeViewModel.registerDevice(CommonUtils.deviceId(contentResolver))
        }
    }

    private fun setViewPager(intent: Intent) {
        val pageOneFragment = TopicsFragment.newInstance().apply {
        }

        val pageTwoFragment = NewsFragment.newInstance(intent).apply {
            setNewsCardChangedListener(cardChangeListener)
        }

        val pageThreeFragment = WebViewFragment.newInstance()

        binding.pager.adapter = PagerAdapter(
            this,
            supportFragmentManager,
            listOf(pageOneFragment, pageTwoFragment, pageThreeFragment)
        )
        binding.pager.currentItem = 1
        binding.indicator.setViewPager(binding.pager)
        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                    }
                    2 -> {

                    }

                }
            }
        })
    }

    private val cardChangeListener = object : NewsFragment.OnNewsCardChangeListener {
        override fun onCardChanged(news: NewsData) {
            when (CardType.parse(news.getCardType().value)) {
                CardType.IntroFb -> {
                    updateWebViewListener?.onUpdateWebView(NewsData(null, CardType.IntroFb, null))
                    binding.indicatorVisible = true
                }
                CardType.Intro -> {
                    updateWebViewListener?.onUpdateWebView(NewsData(null, CardType.Intro, null))
                    binding.indicatorVisible = true
                }
                CardType.News -> {
                    updateWebViewListener?.onUpdateWebView(news)
                    binding.indicatorVisible = true
                }
                CardType.MultiPageAd -> {
                    updateWebViewListener?.onUpdateWebView(news)

                    binding.indicatorVisible = news.getAd()?.images?.size ?: 0 == 1
                }
                CardType.MultiPageParallaxAd -> {
                    updateWebViewListener?.onUpdateWebView(news)
                    binding.indicatorVisible = false
                }
                CardType.VersionUpdate -> {
                    updateWebViewListener?.onUpdateWebView(
                        NewsData(
                            null,
                            CardType.VersionUpdate,
                            null
                        )
                    )
                    binding.indicatorVisible = false
                }
                CardType.Cartoon -> {
                    updateWebViewListener?.onUpdateWebView(news)
                    binding.indicatorVisible = false
                }
                else -> {
                    binding.indicatorVisible = false
                }
            }
        }

        override fun onFooterClicked(selectedNews: NewsData?) {
            binding.pager.currentItem = 2
        }
    }

    override fun onBackPressed() {
        if (binding.pager.currentItem == 1) {
            super.onBackPressed()
        } else {
            binding.pager.currentItem = 1
        }
    }

    private fun firebaseEventLog() {
        firebaseAnalytics.logEvent("installed_via_shared_deeplink", Bundle())
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun reminderNotificationWhenNotUsedRecently() {

        val moreThan7Days = moreThan7Days()
        val notification = Intent(this, AlarmReceiver()::class.java).apply {
            putExtra(AlarmReceiver.SHOW_NOTIFICATION, moreThan7Days)
            putExtra(AlarmReceiver.REMINDER_MESSAGE, firebaseHelper.reminderAlert)
        }
        val repeatingNotification: PendingIntent = PendingIntent.getBroadcast(
            this,
            0, notification, PendingIntent.FLAG_CANCEL_CURRENT
        )
        val alarms: AlarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        alarms.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            AlarmManager.INTERVAL_DAY * 7, repeatingNotification
        )
    }

    private fun moreThan7Days(): Boolean {
        val calendar = Calendar.getInstance().apply {
            time = sharedPrefs.getLastAppOpenedDate().toDateyyyyMMddhhmm();
            add(Calendar.DATE, 10)
        }
        return calendar.time < Calendar.getInstance().time
    }

    companion object {
        private const val TAG = "GoogleActivity"
        const val RC_SIGN_IN = 9001
    }
}
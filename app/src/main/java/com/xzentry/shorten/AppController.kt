package com.xzentry.shorten

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.onesignal.OneSignal
import com.xzentry.shorten.di.component.DaggerAppComponent
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.notification.ShortNewsNotificationOpenedHandler
import com.xzentry.shorten.notification.ShortNewsNotificationReceivedHandler
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import javax.inject.Inject


/*
 * we use our AppComponent (now prefixed with Dagger)
 * to inject our Application class.
 * This way a DispatchingAndroidInjector is injected which is
 * then returned when an injector for an activity is requested.
 * */
class AppController : Application(), HasActivityInjector, HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingAndroidFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    internal lateinit var firebaseHelper: FirebaseHelper

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return dispatchingAndroidActivityInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidFragmentInjector
    }

    @Inject
    internal lateinit var calligraphyConfig: CalligraphyConfig

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)

        //caligraphy latest version requries this
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(calligraphyConfig))
                .build()
        )
        initializeOneSignal()
        initializeFirebase()
    }

    private fun initializeFirebase() {
        firebaseHelper.init()
        firebaseHelper.remoteConfigUpdatesCheck()
    }

    private fun initializeOneSignal() {
        // OneSignal Initialization
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .setNotificationReceivedHandler(
                ShortNewsNotificationReceivedHandler(this)
            )
            .setNotificationOpenedHandler(
                ShortNewsNotificationOpenedHandler(this)
            )
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init();
    }
}

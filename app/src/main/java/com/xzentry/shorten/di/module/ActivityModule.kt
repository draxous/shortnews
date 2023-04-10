package com.xzentry.shorten.di.module

import com.xzentry.shorten.ui.contactus.ContactUsActivity
import com.xzentry.shorten.ui.home.HomeActivity
import com.xzentry.shorten.ui.preview.ImagePreviewActivity
import com.xzentry.shorten.ui.profile.ProfileActivity
import com.xzentry.shorten.ui.search.SearchNewsActivity
import com.xzentry.shorten.ui.selectednews.SelectedNewsActivity
import com.xzentry.shorten.ui.settings.SettingsActivity
import com.xzentry.shorten.ui.webview.WebViewActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector()
    abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector()
    abstract fun contributeSearchNewsActivity(): SearchNewsActivity

    @ContributesAndroidInjector()
    abstract fun contributeSelectedNewsActivity(): SelectedNewsActivity

    @ContributesAndroidInjector()
    abstract fun contributeSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector()
    abstract fun contributeWebViewActivity(): WebViewActivity

    @ContributesAndroidInjector()
    abstract fun contributeImagePreviewActivity(): ImagePreviewActivity

    @ContributesAndroidInjector()
    abstract fun contributeContactUsActivity(): ContactUsActivity

    @ContributesAndroidInjector()
    abstract fun contributeProfileUsActivity(): ProfileActivity
}

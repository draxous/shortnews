package com.xzentry.shorten.di.module

import com.xzentry.shorten.ui.news.NewsFragment
import com.xzentry.shorten.ui.news.multipage.FactCardFragment
import com.xzentry.shorten.ui.selectednews.viewpager.NewsByTopicFragment
import com.xzentry.shorten.ui.topics.TopicsFragment
import com.xzentry.shorten.ui.webview.WebViewFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector()
    abstract fun contributeNewsFragment(): NewsFragment

    @ContributesAndroidInjector()
    abstract fun contributeTopicsFragment(): TopicsFragment

    @ContributesAndroidInjector()
    abstract fun contributeNewsByTopicFragment(): NewsByTopicFragment

    @ContributesAndroidInjector()
    abstract fun contributeWebviewFragment(): WebViewFragment

    @ContributesAndroidInjector()
    abstract fun contributeFactCardFragment(): FactCardFragment

}

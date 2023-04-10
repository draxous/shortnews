package com.xzentry.shorten.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xzentry.shorten.di.ViewModelKey
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.ui.contactus.ContactUsViewModel
import com.xzentry.shorten.ui.home.HomeViewModel
import com.xzentry.shorten.ui.news.NewsViewModel
import com.xzentry.shorten.ui.profile.ProfileViewModel
import com.xzentry.shorten.ui.search.SearchNewsViewModel
import com.xzentry.shorten.ui.selectednews.SelectedNewsViewModel
import com.xzentry.shorten.ui.settings.SettingsViewModel
import com.xzentry.shorten.ui.topics.TopicsViewModel
import com.xzentry.shorten.ui.webview.WebViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
    /*
     * This method basically says
     * inject this object into a Map using the @IntoMap annotation,
     * with the  MovieListViewModel.class as key,
     * and a Provider that will build a MovieListViewModel
     * object.
     *
     * */

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    protected abstract fun homeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectedNewsViewModel::class)
    protected abstract fun selectedNewsViewModel(selectedNewsViewModel: SelectedNewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    protected abstract fun settingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactUsViewModel::class)
    protected abstract fun contactUsViewModel(settingsViewModel: ContactUsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    protected abstract fun profileViewModel(profileViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchNewsViewModel::class)
    protected abstract fun searchNewsViewModel(searchNewsViewModel: SearchNewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel::class)
    protected abstract fun newsViewModel(newsViewModel: NewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TopicsViewModel::class)
    protected abstract fun topicsViewModel(topicsViewModel: TopicsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WebViewModel::class)
    protected abstract fun webViewModel(webViewModel: WebViewModel): ViewModel

}
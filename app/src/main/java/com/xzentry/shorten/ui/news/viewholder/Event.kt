package com.xzentry.shorten.ui.news.viewholder

import android.view.View
import com.xzentry.shorten.data.remote.model.Source
import com.xzentry.shorten.model.NewsData

sealed class Event {

    class OnLoadMoreItems() : Event()
    class OnOpenNetworkSettingsScreen() : Event()
    class OnBackToTop() : Event()
    class OnRateApp() : Event()
    class OnUpdateVersion() : Event()
    class OnCardSkip() : Event()
    class OnPinnedImageClicked(val selectedNewsData: NewsData) : Event()
    class OnShareCard(val shareView: View, val post: NewsData?) : Event()
    class OnOpenSourcePage(val source: Source?) : Event()
}
package com.xzentry.shorten.ui.webview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xzentry.shorten.model.CardType
import com.xzentry.shorten.model.NewsData
import javax.inject.Inject

class WebViewModel @Inject constructor() : ViewModel() {

    private val currentNewsLiveData = MutableLiveData<NewsData>()

    fun updateChangedNewsWebView(selectedTopic: NewsData) {
        currentNewsLiveData.value = selectedTopic
    }

    fun getCurrentNewsLiveData() = currentNewsLiveData
}

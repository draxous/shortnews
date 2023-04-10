package com.xzentry.shorten.ui.search

import androidx.lifecycle.MutableLiveData
import com.xzentry.shorten.common.error
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.dao.TopicDao
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.data.repository.SearchRepository
import com.xzentry.shorten.ui.base.BaseViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class SearchNewsViewModel @Inject constructor(
    postDao: PostDao, movieDao: TopicDao,
    apiService: ApiService, sharedPrefs: PreferencesHelper
) : BaseViewModel<SearchNewsNavigator>() {
    /* You can see we are initialising the MovieRepository class here */
    private val searchRepository: SearchRepository =
        SearchRepository(apiService)

    /* We are using LiveData to update the UI with the data changes.
     */
    private val searchResultsLiveData = MutableLiveData<List<Post?>?>()
    private val searchError = MutableLiveData<String>()

    fun loadNewsByCategoryId(keyword: String) {
        searchRepository.beginSearch(keyword).subscribe(object: Observer<List<Post?>> {
            override fun onSubscribe(d: Disposable) {
                //todo
            }

            override fun onError(e: Throwable) {
                //todo
                searchError.postValue(e.message)
                error("[beginSearch]:$e")
            }

            override fun onNext(data: List<Post?>) {
                if (data.isNotEmpty()) {
                    searchResultsLiveData.postValue(data)
                }else{
                    searchError.postValue("Error occurred, Please try again later")
                }
            }

            override fun onComplete() {
                //isLoading.set(false)
            }
        })

    }

    fun getSearchResultsLiveData() = searchResultsLiveData
    fun getSearchErrorLiveData() = searchError
}

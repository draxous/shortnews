package com.xzentry.shorten.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xzentry.shorten.common.error
import com.xzentry.shorten.data.Resource
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.dao.TopicDao
import com.xzentry.shorten.data.local.perfs.AppPreferencesHelper
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.AdsModel
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.data.repository.AdsRepository
import com.xzentry.shorten.data.repository.PostsRepository
import com.xzentry.shorten.model.NewsData
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class NewsViewModel @Inject constructor(
    postDao: PostDao, movieDao: TopicDao,
    apiService: ApiService, private val sharedPrefs: AppPreferencesHelper
) : ViewModel() {

    private val postsListLiveData = MutableLiveData<Resource<List<Post>>>()
    private val postsListLoadingComplete = MutableLiveData<Boolean>()
    private val loadMorePostsListLiveData = MutableLiveData<Resource<List<Post>>>()
    private val positionForPushNotificationPostIdsLiveData = MutableLiveData<Int>()
    private val currentViewingPostInfo = MutableLiveData<NewsData>()
    private val loginSuccess = MutableLiveData<Boolean>()
    private val allShownPostsListLiveData = MutableLiveData<List<NewsData>>()
    private val adsError = MutableLiveData<String>()
    private val adsResults = MutableLiveData<List<AdsModel?>>()
    private val adsRepository: AdsRepository = AdsRepository(apiService)

    private val postsRepository: PostsRepository =
        PostsRepository(
            postDao,
            apiService,
            sharedPrefs
        )

    val googleSignup: LiveData<Boolean> get() = loginSuccess

    fun setGoogleLoginSuccessStatus(isSuccess: Boolean) {
        loginSuccess.value = isSuccess
    }

    fun loadPosts( networkConnected: Boolean) {
        postsRepository.loadLatestPosts(-1, networkConnected)
            .subscribe(object : Observer<Resource<List<Post>>> {

                override fun onSubscribe(d: Disposable) {
                    //todo
                }

                override fun onError(e: Throwable) {
                    error("[posts]:$e")
                }

                override fun onNext(data: Resource<List<Post>>) {
                    postsListLiveData.value = data
                }

                override fun onComplete() {
                    postsListLoadingComplete.value = true
                }

            })
    }


    fun loadMorePosts(networkConnected: Boolean) {
        postsRepository.loadMoreOlderPosts(networkConnected)
            .subscribe(object : Observer<Resource<List<Post>>> {

                override fun onSubscribe(d: Disposable) {
                    //todo
                }

                override fun onError(e: Throwable) {
                    //todo
                    error("[oldPosts]:$e")
                }

                override fun onNext(data: Resource<List<Post>>) {
                    // repositories.value = data
                    loadMorePostsListLiveData.postValue(data)
                }

                override fun onComplete() {

                }
            })
    }

    fun loadAds() {
        adsRepository.getAds().subscribe(object : Observer<List<AdsModel?>> {
            override fun onSubscribe(d: Disposable) {
                //todo
            }

            override fun onError(e: Throwable) {
                //todo
                adsError.postValue(e.message)
                error("[getAds]:$e")
            }

            override fun onComplete() {
                //isLoading.set(false)

            }

            override fun onNext(data: List<AdsModel?>) {
                if (data.isNotEmpty()) {
                    adsResults.postValue(data)
                } else {
                    adsError.postValue("Error occurred, Please try again later")
                }
            }
        })
    }


    fun getCardPositionForPostId(newsData: List<NewsData>, postId: Int) {
        positionForPushNotificationPostIdsLiveData.postValue(newsData.indexOfFirst { it.getPostItem()?.id == postId })
    }

    //saves current card info and used to load webview on
    fun getCurrentCardInfo() = currentViewingPostInfo
    //keeps all the loaded posts in the posts deck
    fun getAllShownPostsLiveData() = allShownPostsListLiveData
    fun getPostsLiveData() = postsListLiveData
    fun getPostsListLoadingCompleteLiveData() = postsListLoadingComplete
    fun getLoadMorePostsLiveData() = loadMorePostsListLiveData
    fun getPositionForPushNotificationPostIdsLiveData() = positionForPushNotificationPostIdsLiveData
    fun getAds() = adsResults
    fun getGoogleLoginSuccessLiveData() = loginSuccess
}

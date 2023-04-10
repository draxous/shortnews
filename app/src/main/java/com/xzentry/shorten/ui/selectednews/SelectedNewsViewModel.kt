package com.xzentry.shorten.ui.selectednews

import androidx.lifecycle.MutableLiveData
import com.xzentry.shorten.common.error
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.dao.TopicDao
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.data.repository.PostsRepository
import com.xzentry.shorten.ui.base.BaseViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class SelectedNewsViewModel @Inject constructor(
    postDao: PostDao, topicDao: TopicDao,
    movieApiService: ApiService, sharedPrefs: PreferencesHelper
) : BaseViewModel<SelectedNewsNavigator>() {
    /* You can see we are initialising the MovieRepository class here */
    private val postsRepository: PostsRepository =
        PostsRepository(postDao, movieApiService, sharedPrefs)

    /* We are using LiveData to update the UI with the data changes.
     */
    private val postsByTopicListLiveData = MutableLiveData<List<Post?>>()
    private val loadMorePostsListLiveData = MutableLiveData<List<Post?>>()
    private val errorLoadingData = MutableLiveData<Boolean>()
    private val currentViewingPostInfo = MutableLiveData<Post>()
    private val searchPostClickedPositionLiveData = MutableLiveData<Int>()
    private val selectedTopicLiveData = MutableLiveData<Post>()

    fun loadNewsByCategoryId(topicId: Int) {
        postsRepository.loadPostsByTopic(topicId).subscribe(object: Observer<List<Post?>> {
            override fun onSubscribe(d: Disposable) {
                //todo
            }

            override fun onError(e: Throwable) {
                //todo
               // searchError.postValue(e.message)
                error("[loadPostsByTopic]:$e")
            }

            override fun onNext(data: List<Post?>) {
                if (data.isNotEmpty()) {
                    postsByTopicListLiveData.postValue(data)
                }else{
                    errorLoadingData.postValue(true)
                }

            }

            override fun onComplete() {
                //isLoading.set(false)
            }
        })
    }

    fun loadNewsBySourceId(sourceId: Int) {
        postsRepository.loadNewsBySourceId(sourceId).subscribe(object: Observer<List<Post?>> {
            override fun onSubscribe(d: Disposable) {
                //todo
            }

            override fun onError(e: Throwable) {
                //todo
               // searchError.postValue(e.message)
                error("[loadPostsByTopic]:$e")
            }

            override fun onNext(data: List<Post?>) {
                if (data.isNotEmpty()) {
                    postsByTopicListLiveData.postValue(data)
                }else{
                    errorLoadingData.postValue(true)
                }

            }

            override fun onComplete() {
                //isLoading.set(false)
            }
        })
    }

    fun loadMorePosts(topicId: Int, lastPostUpdatedAt: String) {
        postsRepository.loadMoreOlderPostsByTopic(topicId , lastPostUpdatedAt)
            .subscribe(object: Observer<List<Post?>> {

                override fun onSubscribe(d: Disposable) {
                    //todo
                }

                override fun onError(e: Throwable) {
                    //todo
                    error("[oldPosts]:$e")
                }

                override fun onComplete() {

                }

                override fun onNext(data: List<Post?>) {
                    loadMorePostsListLiveData.postValue(data)
                }

            })
    }

    fun updateClickedPosition(position: Int) {
        searchPostClickedPositionLiveData.postValue(position)
    }


    fun getSelectedTopicLiveData() = selectedTopicLiveData
    fun getSearchPostClickedPositionLiveData() = searchPostClickedPositionLiveData
    fun getPostsByTopicLiveData() = postsByTopicListLiveData
    fun getLoadMorePostsLiveData() = loadMorePostsListLiveData
    fun getErrorLoadingLiveData() = errorLoadingData
}

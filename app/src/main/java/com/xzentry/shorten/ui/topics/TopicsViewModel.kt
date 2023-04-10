package com.xzentry.shorten.ui.topics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xzentry.shorten.common.error
import com.xzentry.shorten.data.Resource
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.dao.TopicDao
import com.xzentry.shorten.data.local.perfs.AppPreferencesHelper
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.Topic
import com.xzentry.shorten.data.repository.TopicRepository
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class TopicsViewModel @Inject constructor(
    postDao: PostDao, movieDao: TopicDao,
    apiService: ApiService, private val sharedPrefs: AppPreferencesHelper
) : ViewModel() {

    private val topicsListLiveData = MutableLiveData<Resource<List<Topic>>>()

    private val topicsRepository: TopicRepository =
        TopicRepository(
            movieDao,
            apiService
        )

    fun loadTopics() {
        topicsRepository.loadCategories()
            .subscribe(object : Observer<Resource<List<Topic>>> {

                override fun onSubscribe(d: Disposable) {
                    //todo
                }

                override fun onError(e: Throwable) {
                    //todo
                    error("[categories]:$e")
                }

                override fun onNext(data: Resource<List<Topic>>) {
                    if (data.data?.isNotEmpty() == true) {
                        topicsListLiveData.postValue(data)
                    }
                }

                override fun onComplete() {

                }

            })
    }

    fun getTopicsLiveData() = topicsListLiveData
}

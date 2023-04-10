package com.xzentry.shorten.data.repository


import com.xzentry.shorten.data.NetworkBoundResource
import com.xzentry.shorten.data.Resource
import com.xzentry.shorten.data.local.dao.TopicDao
import com.xzentry.shorten.data.local.toTopicEntityList
import com.xzentry.shorten.data.local.toTopicList
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.Topic
import com.xzentry.shorten.data.remote.model.response.CategoryApiResponse
import io.reactivex.Flowable
import io.reactivex.Observable
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TopicRepository @Inject constructor(
    private val categoryDao: TopicDao,
    private val apiService: ApiService
) {
    fun loadCategories(): Observable<Resource<List<Topic>>> {
        return object : NetworkBoundResource<List<Topic>, CategoryApiResponse>() {

            override fun saveCallResult(item: CategoryApiResponse) {
                categoryDao.deleteTopics()
                categoryDao.insertTopics(item.data.toTopicEntityList())
            }

            override fun shouldFetch(): Boolean {
                return true
            }

            override fun loadFromDb(): Flowable<List<Topic>> {
                val movieEntities = categoryDao.getTopics()
                return if (movieEntities == null || movieEntities.isEmpty()) {
                    Flowable.empty()
                } else Flowable.just(movieEntities.toTopicList())
            }

            override fun createCall(): Observable<Resource<CategoryApiResponse>> {
                return apiService.fetchCategories()
                    .flatMap { topicsApiResponse ->
                        Observable.just(
                            if (topicsApiResponse.status != HttpURLConnection.HTTP_OK) Resource.error(
                                "",
                                CategoryApiResponse(
                                    emptyList(),
                                    0
                                )

                            )
                            else Resource.success(topicsApiResponse)
                        )
                    }
            }
        }.getAsObservable()
    }
}

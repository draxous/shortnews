package com.xzentry.shorten.data.repository


import com.xzentry.shorten.data.NetworkBoundResource
import com.xzentry.shorten.data.Resource
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.data.local.toPostEntityList
import com.xzentry.shorten.data.local.toPostList
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.data.remote.model.response.PostApiResponse
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton


/*
 * One of the first things we do in the Repository class
 * is to make it a Singleton.
 * */

@Singleton
class PostsRepository(
    private val postDao: PostDao,
    private val apiService: ApiService, private val sharedPrefs: PreferencesHelper
) {

    /*
         * We are using this method to fetch the posts list
         * NetworkBoundResource is part of the Android architecture
         * components. You will notice that this is a modified version of
         * that class. That class is based on LiveData but here we are
         * using Observable from RxJava.
         *
         * There are three methods called:
         * a. fetch data from server
         * b. fetch data from local
         * c. save data from api in local
         *
         * So basically we fetch data from server, store it locally
         * and then fetch data from local and update the UI with
         * this data.
         *
         * */
    fun loadLatestPosts(
        cat_id: Int = -1,networkConnected: Boolean
    ): Observable<Resource<List<Post>>> {
        return object : NetworkBoundResource<List<Post>, PostApiResponse>() {

            override fun createCall(): Observable<Resource<PostApiResponse>> {
                return apiService.fetchLatestPosts(
                    "sin",
                    1,
                    cat_id,
                    sharedPrefs.getLatestNewsPostUpdatedAtTime() ?: "",
                    -1
                )
                    .flatMap { postApiResponse ->
                        Observable.just(
                            if (postApiResponse.status != HttpURLConnection.HTTP_OK) {
                                Resource.error("",
                                    PostApiResponse(
                                        emptyList(),
                                        0
                                    )
                                )
                            } else {
                                Resource.success(postApiResponse)
                            }
                        )
                    }
            }

            override fun saveCallResult(postApiResponse: PostApiResponse) {
                postDao.insertPosts(postApiResponse.data.toPostEntityList())

                // delete will happen only once per day
                deleteOldDataOncePerDay()
            }

            override fun shouldFetch(): Boolean {
                return networkConnected
            }

            override fun loadFromDb(): Flowable<List<Post>> {

                var posts: List<Post> = if (shouldFetch()) {
                    //posts that are fetched from network for the last time
                    postDao.loadLatestNewPosts(sharedPrefs.getOldestLoadedNewsPostUpdatedAtTime() ?: "").toPostList()

                } else {
                    //posts that are already saved in db
                    postDao.loadRecentPosts().toPostList()
                }

                //set last and first post updateAt times
                updatePostDateTimeFlags(posts)

                return Flowable.just(posts)
            }

        }.getAsObservable()
    }

    private fun updatePostDateTimeFlags(posts: List<Post>) {
        if (posts.isNotEmpty()) {

            posts.first().updatedAt?.let {
                sharedPrefs.setLatestNewsPostUpdatedAtTime(it)
            }

            posts.last().updatedAt?.let {
                sharedPrefs.setOldestLoadedNewsPostUpdatedAtTime(it)
            }
        }
    }

    private fun deleteOldDataOncePerDay() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -15)
        val date = Date()
        var osLocalizedDateFormat = SimpleDateFormat("yyyy-MM-dd k:mm:ss")

        // delete will happen only once per day
        if (sharedPrefs.getDeletedDate()?.compareTo(date.date.toString()) != 0) {
            postDao.deleteAll(osLocalizedDateFormat.format(calendar.time))
            sharedPrefs.setDeletedDate(date.date.toString())
        }
    }

    fun loadMoreOlderPosts(
        networkConnected: Boolean,
        loadAfterServer: Boolean = false
    ): Observable<Resource<List<Post>>> {
        var loadAfterDataLoadedFromServer: Boolean = loadAfterServer
        return object : NetworkBoundResource<List<Post>, PostApiResponse>() {

            override fun createCall(): Observable<Resource<PostApiResponse>> {
                return apiService.fetchOlderPosts(
                    "sin",
                    1,
                    -1,
                    sharedPrefs.getOldestLoadedNewsPostUpdatedAtTime() ?: ""
                )
                    .flatMap { postApiResponse ->
                        loadAfterDataLoadedFromServer = true
                        Observable.just(
                            if (postApiResponse.status != HttpURLConnection.HTTP_OK) {

                                Resource.error(
                                    "",
                                    PostApiResponse(
                                        emptyList(),
                                        0
                                    )
                                )

                            } else {
                                Resource.success(postApiResponse)
                            }
                        )
                    }
            }

            override fun saveCallResult(item: PostApiResponse) {

                postDao.insertPosts(item.data.toPostEntityList())

            }

            override fun shouldFetch(): Boolean {
                return true
            }

            override fun loadFromDb(): Flowable<List<Post>> {

                var posts: List<Post>? = null

                //posts that are fetched from network for the last time
                if (networkConnected && loadAfterDataLoadedFromServer) {
                    posts =
                        postDao.loadMoreOlderPosts(
                            sharedPrefs.getOldestLoadedNewsPostUpdatedAtTime() ?: ""
                        ).toPostList()

                    if (posts.isNotEmpty()) {
                        posts.last().updatedAt?.let {
                            sharedPrefs.setOldestLoadedNewsPostUpdatedAtTime(it)
                        }
                    }
                }

                if (posts == null || posts.isEmpty()) {
                    posts = emptyList()
                }
                return Flowable.just(posts)
            }
        }.getAsObservable()
    }

    internal fun loadPostsByTopic(topicId: Int): Observable<List<Post?>> {

        return apiService.fetchLatestPosts("sin", 1, topicId, "-1",-1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { postApiResponse ->
                Observable.just(
                    if (postApiResponse.status != HttpURLConnection.HTTP_OK) {
                        PostApiResponse(
                            emptyList(),
                            0
                        ).data
                    } else {
                        postApiResponse.data
                    }
                )
            }
    }

    internal fun loadNewsBySourceId(sourceId: Int): Observable<List<Post?>> {

        return apiService.fetchLatestPosts("sin", 1, -1, "-1", sourceId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { postApiResponse ->
                Observable.just(
                    if (postApiResponse.status != HttpURLConnection.HTTP_OK) {
                        PostApiResponse(
                            emptyList(),
                            0
                        ).data
                    } else {
                        postApiResponse.data
                    }
                )
            }
    }

    internal fun loadMoreOlderPostsByTopic(
        topicId: Int,
        lastPostUpdatedAt: String
    ): Observable<List<Post?>> {

        return apiService.fetchLatestPosts("sin", 1, topicId, lastPostUpdatedAt, -1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { postApiResponse ->
                Observable.just(
                    if (postApiResponse.status != HttpURLConnection.HTTP_OK) {
                        PostApiResponse(
                            emptyList(),
                            0
                        ).data
                    } else {
                        postApiResponse.data
                    }
                )
            }
    }
}

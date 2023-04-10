package com.xzentry.shorten.data.repository


import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.data.remote.model.response.PostApiResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import java.net.HttpURLConnection
import javax.inject.Singleton

@Singleton
class SearchRepository(private val apiService: ApiService) {

    internal fun beginSearch(keyword: String): Observable<List<Post?>> {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("keyword", keyword)
            .build()
        return apiService.searchPosts(body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { searchApiResponse ->
                Observable.just(
                    if (searchApiResponse.status != HttpURLConnection.HTTP_OK) {
                        PostApiResponse(
                            emptyList(),
                            0
                        ).data
                    } else {
                        searchApiResponse.data?: PostApiResponse(
                            emptyList(),
                            0
                        ).data
                    }
                )
            }
    }
}

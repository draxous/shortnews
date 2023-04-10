package com.xzentry.shorten.data.repository


import com.xzentry.shorten.data.remote.model.AdsModel
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.response.AdsApiResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import javax.inject.Singleton


/*
 * One of the first things we do in the Repository class
 * is to make it a Singleton.
 * */

@Singleton
class AdsRepository(private val apiService: ApiService) {

    internal fun getAds(): Observable<List<AdsModel?>> {
        return apiService.fetchAds()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { adsApiResponse ->
                Observable.just(
                    if (adsApiResponse.status != HttpURLConnection.HTTP_OK) {
                        AdsApiResponse(
                            emptyList(),
                            "",
                            0
                        ).data
                    } else {
                        adsApiResponse.data
                    }
                )
            }
    }
}

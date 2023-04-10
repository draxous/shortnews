package com.xzentry.shorten.data.repository


import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.response.LoginApiResponse
import com.xzentry.shorten.data.remote.model.LoginInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import java.net.HttpURLConnection
import javax.inject.Singleton


/*
 * One of the first things we do in the Repository class
 * is to make it a Singleton.
 * */

@Singleton
class LoginRepository(
    private val apiService: ApiService, private val sharedPrefs: PreferencesHelper
) {

    internal fun registerDevice(deviceId: String): Observable<LoginApiResponse> {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("device", deviceId)
            .build()
        return apiService.createUser(body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { loginApiResponse ->
                Observable.just(
                    if (loginApiResponse.status != HttpURLConnection.HTTP_OK) {
                        LoginApiResponse(
                            LoginInfo(null, null, null, null, null, null),
                            0
                        )

                    } else {
                        loginApiResponse
                    }
                )
            }
    }
}

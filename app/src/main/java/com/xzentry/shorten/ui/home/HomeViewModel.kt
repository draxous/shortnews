package com.xzentry.shorten.ui.home

import com.xzentry.shorten.common.error
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.dao.TopicDao
import com.xzentry.shorten.data.local.perfs.AppPreferencesHelper
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.model.response.LoginApiResponse
import com.xzentry.shorten.data.repository.LoginRepository
import com.xzentry.shorten.ui.base.BaseViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    postDao: PostDao, movieDao: TopicDao,
    apiService: ApiService, private val sharedPrefs: AppPreferencesHelper
) : BaseViewModel<HomeNavigator>() {


    private val loginRepository: LoginRepository =
        LoginRepository(
            apiService,
            sharedPrefs
        )

    fun registerDevice(deviceId: String) {
        if (!sharedPrefs.isDeviceRegistered()) {
            loginRepository.registerDevice(deviceId).subscribe(object : Observer<LoginApiResponse> {
                override fun onSubscribe(d: Disposable) {
                    //todo
                }

                override fun onError(e: Throwable) {
                    //todo
                    error("[createUser]:$e")
                }

                override fun onNext(data: LoginApiResponse) {
                    data.let {
                        sharedPrefs.registerDevice(true)
                        sharedPrefs.setDeviceId(deviceId)
                    }
                }

                override fun onComplete() {
                    //isLoading.set(false)
                }
            })
        }
    }
}

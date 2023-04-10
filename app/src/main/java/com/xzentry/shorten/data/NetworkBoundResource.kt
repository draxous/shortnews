package com.xzentry.shorten.data

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class NetworkBoundResource<ResultType, RequestType> @MainThread
protected constructor() {

    private val asObservable: Observable<Resource<ResultType>>

    init {
        val source: Observable<Resource<ResultType>>
        if (shouldFetch()) {

            source = createCall()
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    saveCallResult(processResponse(it)!!) //save on local db
                }

                .flatMap {
                    loadFromDb().toObservable()
                        .map { Resource.success(it) }
                }

                .doOnError { onFetchFailed() }

                .onErrorResumeNext { t: Throwable ->
                    loadFromDb().toObservable().map {
                        Resource.error(t.message!!, it)
                    }
                }

                .observeOn(AndroidSchedulers.mainThread())

        } else {
            source = loadFromDb()
                .toObservable()
                .map { Resource.success(it) }
        }

        asObservable = Observable.concat(
            loadFromDb()
                .toObservable()
                .map { Resource.loading(it) }
                .take(1),
            source
        )
    }

    fun getAsObservable(): Observable<Resource<ResultType>> {
        return asObservable
    }

    private fun onFetchFailed() {}

    @WorkerThread
    protected fun processResponse(response: Resource<RequestType>): RequestType? {
        return response.data
    }

    //This is where we will use Room’s DAO to save locally whatever data we just fetched from the server
    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    //This determines whether the app should fetch new
    // data from the server and update the data already stored on our local device database.
    @MainThread
    protected abstract fun shouldFetch(): Boolean

    //This method loads the data saved locally in Room
    @MainThread
    protected abstract fun loadFromDb(): Flowable<ResultType>
    //Here we will call a function that will fetch data from the server
    @MainThread
    protected abstract fun createCall(): Observable<Resource<RequestType>>
}
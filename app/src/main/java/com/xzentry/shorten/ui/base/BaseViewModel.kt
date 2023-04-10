package com.xzentry.shorten.ui.base

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import java.lang.ref.WeakReference


abstract class BaseViewModel<N>: ViewModel() {

    private val isLoading = ObservableBoolean(false)

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private lateinit var mNavigator: WeakReference<N>

    var navigator: N
        get() = mNavigator?.get()!!
        set(navigator) {
            this.mNavigator = WeakReference(navigator)
        }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun setIsLoading(isLoading: Boolean) {
        this.isLoading.set(isLoading)
    }
}
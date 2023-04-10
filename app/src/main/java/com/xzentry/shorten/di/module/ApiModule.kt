package com.xzentry.shorten.di.module

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.AppPreferencesHelper
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.data.remote.api.ApiService
import com.xzentry.shorten.data.remote.interceptor.FirebasePerformanceInterceptor
import com.xzentry.shorten.data.remote.interceptor.RequestInterceptor
import com.xzentry.shorten.ui.news.storage.StorageHelper
import com.xzentry.shorten.ui.news.storage.StorageHelperImpl
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.FirebaseHelperImpl
import com.xzentry.shorten.utils.network.ConnectivityChecker
import dagger.Module
import dagger.Provides
import io.github.inflationx.calligraphy3.CalligraphyConfig
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
class ApiModule {

    /*
     * The method returns the Gson object
     * */
    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.create()
    }


    /*
     * The method returns the Cache object
     * */
    @Provides
    @Singleton
    internal fun provideCache(application: Application): Cache {
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        val httpCacheDirectory = File(application.cacheDir, "http-cache")
        return Cache(httpCacheDirectory, cacheSize)
    }


    /*
     * The method returns the Okhttp object
     * */
    @Provides
    @Singleton
    internal fun provideOkhttpClient(cache: Cache): OkHttpClient {
        return OkHttpClient.Builder().run {
            cache(cache)

            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                addInterceptor(logging)
            }

            addInterceptor(FirebasePerformanceInterceptor())
            addNetworkInterceptor(RequestInterceptor())
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)

            dispatcher(Dispatcher().apply {
                maxRequestsPerHost = 15
            })
            build()
        }
    }


    /*
     * The method returns the Retrofit object
     * */
    @Provides
    @Singleton
    internal fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    internal fun provideCalligraphyDefaultConfig(): CalligraphyConfig {
        return CalligraphyConfig.Builder()
            .setDefaultFontPath("fonts/google-sans/ProductSans-Regular.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
    }

    @Provides
    @Singleton
    internal fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    internal fun providesFirebaseHelper(firebaseHelperImpl: FirebaseHelperImpl): FirebaseHelper {
        return firebaseHelperImpl
    }

    @Provides
    @Singleton
    internal fun providesStorageHelper(storageHelperHelperImpl: StorageHelperImpl): StorageHelper {
        return storageHelperHelperImpl
    }

    @Provides
    @Singleton
    internal fun provideAppPreferencesHelper(context: Context): AppPreferencesHelper {
        return AppPreferencesHelper(context)
    }

    @Provides
    @Singleton
    internal fun providePreferencesHelper(appPreferencesHelper: AppPreferencesHelper): PreferencesHelper {
        return appPreferencesHelper
    }

    /*
     * We need the MovieApiService module.
     * For this, We need the Retrofit object, Gson, Cache and OkHttpClient .
     * So we will define the providers for these objects here in this module.
     *
     * */
    @Provides
    @Singleton
    internal fun provideMovieApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    internal fun providesConnectivityChecker(context: Context): ConnectivityChecker {
        return ConnectivityChecker(context)
    }
}

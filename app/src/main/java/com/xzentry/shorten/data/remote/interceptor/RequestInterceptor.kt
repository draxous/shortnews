package com.xzentry.shorten.data.remote.interceptor


import com.xzentry.shorten.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val url = originalUrl.newBuilder()
            .build()

        val requestBuilder = originalRequest.newBuilder().url(url).apply {
            addHeader("AuthKey", BuildConfig.AUTH_KEY)
            addHeader("Accept", "*/*")
            addHeader("Content-Type", "multipart/form-data")
            addHeader("Accept-Encoding", "gzip,deflate,br")
            addHeader("Connection", "keep-alive")
        }
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

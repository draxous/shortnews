package com.xzentry.shorten.data.remote.interceptor

import com.google.firebase.perf.FirebasePerformance
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.math.min

internal class FirebasePerformanceInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // https://firebase.google.com/docs/perf-mon/https_traces?platform=android
        val url = request.url.toUrl()
        val httpMetric = FirebasePerformance.getInstance().newHttpMetric(url,
            request.method)

        httpMetric.start()

        val response: Response = chain.proceed(request)
        //Attribute value length must not exceed 100 characters
        httpMetric.putAttribute("api_endpoint", request.url.toString().substring(0, min(request.url.toString().length, 100)))
        httpMetric.setHttpResponseCode(response.code)

        httpMetric.stop()
        return response
    }
}
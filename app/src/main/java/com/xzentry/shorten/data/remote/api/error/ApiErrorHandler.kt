package com.xzentry.shorten.data.remote.api.error

interface ApiErrorHandler {

    fun handleError(url: String, e: Exception): Boolean

    fun handleApiError(url: String, code: Int, errors: List<ApiError>): Boolean
}
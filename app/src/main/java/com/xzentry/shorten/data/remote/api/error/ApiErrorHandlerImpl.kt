package com.xzentry.shorten.data.remote.api.error


import com.xzentry.shorten.AppController
import java.net.HttpURLConnection
import javax.inject.Inject

internal class ApiErrorHandlerImpl @Inject constructor(private val app: AppController) : ApiErrorHandler {

    override fun handleApiError(url: String, code: Int, errors: List<ApiError>): Boolean {
        //logger.logException(ApiFailureException(url, code, errorBody = ApiFailureResponse(errors)))

        val errorPromptType = when (code) {
            HttpURLConnection.HTTP_BAD_REQUEST -> {

            }
            HttpURLConnection.HTTP_NOT_FOUND -> {

            }
            HttpURLConnection.HTTP_BAD_METHOD -> {}
            HttpURLConnection.HTTP_INTERNAL_ERROR -> {}
            HttpURLConnection.HTTP_UNAVAILABLE ->{}
            HttpURLConnection.HTTP_GATEWAY_TIMEOUT ->{}
            else -> {

            }

        }

       // app.notifyApiError(errorPromptType)

        return true
    }

    private fun handleAuthError() {
       // app.notifyAuthError()
    }

    override fun handleError(url: String, e: Exception): Boolean {
        /*if (e is ApiFailureException) {
            handleApiError(url, e.responseCode, e.errorBody?.errors ?: emptyList())
        } else {
            var loggedException = e

            when (e) {
                is AuthExpiredException -> handleAuthError()
                is AuthRequestFailureException -> {} // Not taking any action for this request, since action was already taken for the auth request
                else -> {
                    val errorPromptType = when (e) {
                        is UnknownHostException -> ErrorPromptType.Snackbar(R.string.error_dialog_not_connected_message)
                        is SocketTimeoutException -> ErrorPromptType.Snackbar(R.string.error_dialog_not_connected_message)
                        is ConnectException -> ErrorPromptType.Snackbar(R.string.error_dialog_not_connected_message)
                        else -> ErrorPromptType.Snackbar(R.string.error_dialog_unknown)
                    }

                    app.notifyApiError(errorPromptType)
                    loggedException = ApiFailureException(url, 0, e)
                }
            }

            logger.logException(loggedException)
        }*/

        return true
    }
}
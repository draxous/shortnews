package com.xzentry.shorten.data.remote.model.response

import com.google.gson.annotations.SerializedName
import com.xzentry.shorten.data.remote.model.LoginInfo

data class LoginApiResponse(

    @field:SerializedName("data")
	val data: LoginInfo? = null,

    @field:SerializedName("status")
	val status: Int,

    @field:SerializedName("message")
	val message: String?= null
)
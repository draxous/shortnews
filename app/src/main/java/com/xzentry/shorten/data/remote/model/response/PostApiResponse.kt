package com.xzentry.shorten.data.remote.model.response

import com.google.gson.annotations.SerializedName
import com.xzentry.shorten.data.remote.model.Post

data class PostApiResponse(

	@field:SerializedName("data")
	val data: List<Post>?= null,

	@field:SerializedName("status")
	val status: Int,

	@field:SerializedName("message")
	val message: String?= null
)
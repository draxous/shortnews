package com.xzentry.shorten.data.remote.model.response

import com.google.gson.annotations.SerializedName
import com.xzentry.shorten.data.remote.model.Post

data class SearchApiResponse(

	@field:SerializedName("data")
	val data: List<Post?>? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("message")
	val message: String?= null
)
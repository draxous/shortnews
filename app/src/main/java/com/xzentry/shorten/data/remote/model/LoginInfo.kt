package com.xzentry.shorten.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginInfo(

	@field:SerializedName("user_type")
	val userType: Int? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("last_name")
	val lastName: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
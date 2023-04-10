package com.xzentry.shorten.data.remote.model.response

import com.google.gson.annotations.SerializedName
import com.xzentry.shorten.data.remote.model.Topic

data class CategoryApiResponse(

    @field:SerializedName("data")
    val data: List<Topic>,

    @field:SerializedName("status")
    val status: Int,

    @field:SerializedName("message")
    val message: String? = null
)
package com.xzentry.shorten.data.remote.model.response

import com.google.gson.annotations.SerializedName
import com.xzentry.shorten.data.remote.model.AdsModel

data class AdsApiResponse(

    @field:SerializedName("data")
    val data: List<AdsModel?>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
)
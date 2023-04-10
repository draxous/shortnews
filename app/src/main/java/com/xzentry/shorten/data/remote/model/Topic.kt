package com.xzentry.shorten.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Topic(
    @field:SerializedName("image_url")
    val catImageUrl: String?,

    @field:SerializedName("id")
    val catId: Int,

    @field:SerializedName("category")
    val category: String?,

    @field:SerializedName("updated_at")
    val topicUpdatedAt: String?

) : Parcelable

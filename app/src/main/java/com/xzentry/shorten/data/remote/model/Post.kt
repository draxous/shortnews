package com.xzentry.shorten.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    @field:SerializedName("sub_title")
    var subTitle: String?,

    @field:SerializedName("image_url")
    var imageUrl: String?,

    @field:SerializedName("id")
    var id: Int,

    @field:SerializedName("source")
    var source: Source? = null,

    @field:SerializedName("title")
    var title: String?,

    @field:SerializedName("category")
    var category: Topic? = null,

    @field:SerializedName("content")
    var content: String?,

    @field:SerializedName("source_url")
    var sourceUrl: String?,

    @field:SerializedName("notification")
    var notification: Int,

    @field:SerializedName("updated_at")
    var updatedAt: String?,

    @field:SerializedName("video_url")
    var videoUrl: String?,

    @field:SerializedName("comments_count")
    var commentsCount: String?

) : Parcelable
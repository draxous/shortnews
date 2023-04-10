package com.xzentry.shorten.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AdsModel(

    @field:SerializedName("background_image")
    val backgroundImage: String? = null,

    @field:SerializedName("images")
    val images: List<AdImage?>? = null,

    @field:SerializedName("ad")
    val ad: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("position")
    val position: Int? = null,

    @field:SerializedName("type")
    val type: Int? = null,

    @field:SerializedName("redirect_url")
    val redirectUrl: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
) : Parcelable

@Parcelize
data class AdImage(

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("id")
    val id: Int? = null
) : Parcelable
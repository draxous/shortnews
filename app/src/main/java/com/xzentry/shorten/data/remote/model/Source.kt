package com.xzentry.shorten.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Source(

    @field:SerializedName("id")
    val sourceId: Int,

    @field:SerializedName("source")
    val source: String?
) : Parcelable
package com.xzentry.shorten.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SourceEntity(
    @PrimaryKey
    val sourceId: Int,
    val source: String?
)

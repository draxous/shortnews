package com.xzentry.shorten.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class TopicEntity(
    val catImageUrl: String?,
    @PrimaryKey
    val catId: Int,
    val category: String?,
    val topicUpdatedAt: String?
)

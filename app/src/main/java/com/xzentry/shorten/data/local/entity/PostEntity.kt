package com.xzentry.shorten.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostEntity(
    var subTitle: String?,
    var imageUrl: String?,
    @PrimaryKey
    var id: Int,
    @Embedded
    var source: SourceEntity? = null,
    var title: String?,
    @Embedded
    var category: TopicEntity? = null,
    var content: String?,
    var sourceUrl: String?,
    var notification: Int,
    var updatedAt: String?,
    var videoUrl: String?,
    var commentsCount: String?

)
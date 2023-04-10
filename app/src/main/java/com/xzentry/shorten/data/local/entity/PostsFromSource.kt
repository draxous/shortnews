package com.xzentry.shorten.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class PostsFromSource(

	@Embedded val source: SourceEntity,
	@Relation(
		parentColumn = "cat_id",
		entityColumn = "cat_id"
	)
	val posts: List<PostEntity>
)
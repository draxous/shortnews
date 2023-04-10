package com.xzentry.shorten.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xzentry.shorten.data.local.entity.PostEntity


@Dao
interface PostDao {

    /* Method to insert the movies fetched from api
     * to room */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: List<PostEntity?>?): LongArray

    /* Method to fetch the movies stored locally */
    @Query("SELECT * FROM `PostEntity` WHERE updatedAt > :lastPostUpdatedAt ORDER BY updatedAt DESC LIMIT 50")
    fun loadLatestNewPosts(lastPostUpdatedAt: String): List<PostEntity>

    /* Method to fetch the movies stored locally */
    @Query("SELECT * FROM `PostEntity` WHERE updatedAt < :lastPostUpdatedAt ORDER BY updatedAt DESC LIMIT 40")
    fun loadMoreOlderPosts(lastPostUpdatedAt: String): List<PostEntity>

    /* Method to fetch the movies stored locally */
    @Query("SELECT * FROM `PostEntity` ORDER BY updatedAt DESC LIMIT 40")
    fun loadRecentPosts(): List<PostEntity>

    @Query("SELECT * FROM `PostEntity` WHERE catId = :topicId ORDER BY updatedAt DESC")
    fun getPostsByTopic(topicId: Int): List<PostEntity>?

    @Query("DELETE FROM `PostEntity` where updatedAt < :dataDeleteThreshold")
    fun deleteAll(dataDeleteThreshold: String)
}
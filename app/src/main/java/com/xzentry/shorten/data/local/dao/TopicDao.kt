package com.xzentry.shorten.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xzentry.shorten.data.local.entity.TopicEntity

@Dao
interface TopicDao {

    /* Method to insert the categories fetched from api
     * to room */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTopics(movies: List<TopicEntity>): LongArray

    /* Method to insert the categories fetched from api
     * to room */
    @Query("DELETE FROM TopicEntity")
    fun deleteTopics()

    /* Method to fetch the categories stored locally */
    @Query("SELECT * FROM TopicEntity ORDER BY topicUpdatedAt ASC ")
    fun getTopics(): List<TopicEntity>
}
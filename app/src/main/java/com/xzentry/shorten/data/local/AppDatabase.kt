package com.xzentry.shorten.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.dao.TopicDao
import com.xzentry.shorten.data.local.entity.PostEntity
import com.xzentry.shorten.data.local.entity.TopicEntity

@Database(entities = [TopicEntity::class, PostEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun topicsDao(): TopicDao
    abstract fun postDao(): PostDao
}

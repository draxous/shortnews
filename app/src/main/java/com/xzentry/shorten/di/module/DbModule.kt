package com.xzentry.shorten.di.module

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xzentry.shorten.data.local.AppDatabase
import com.xzentry.shorten.data.local.dao.PostDao
import com.xzentry.shorten.data.local.dao.TopicDao
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
class DbModule {

    /*
     * The method returns the Database object
     * */
    @Provides
    @Singleton
    internal fun provideDatabase(
        application: Application,
        migrations: Set<@JvmSuppressWildcards Migration>
    ): AppDatabase {
        return Room.databaseBuilder(
            application, AppDatabase::class.java, "ShortNews.db"
        )
            .addMigrations(*migrations.toTypedArray()).fallbackToDestructiveMigration()
            .allowMainThreadQueries().build()
    }

    /**
     * Database migration to update from version 1 to 2.
     * Version 2 adds field [PostEntity.topicUpdatedAt]
     */

    @Provides
    @IntoSet
    @Singleton
    fun provide1To2Migration(): Migration {
        return object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //not migration script available because all changes were related to fields allowing null
                // which db is already supported
                //https://medium.com/@prithvibhola08/room-migration-limitation-to-alter-table-49e5e43bca29
                database.run {
                    try {
                        beginTransaction()
                        // Create temp topics table
                        execSQL(
                            "CREATE TABLE TopicEntityTmp (catImageUrl TEXT  NULL, catId INTEGER NOT NULL, category TEXT  NULL, topicUpdatedAt TEXT NULL, PRIMARY KEY(catId))"
                        )

                        // Remove the old table
                        execSQL("DROP TABLE TopicEntity")

                        // Change the table name to the correct one
                        execSQL("ALTER TABLE TopicEntityTmp RENAME TO TopicEntity")
                        setTransactionSuccessful()
                    } finally {
                        endTransaction()
                    }
                }
            }
        }
    }

    /**
     * Database migration to update from version 2 to 3.
     * Version 3 adds field [PostEntity.videoUrl] and [PostEntity.commentsCount].
     */

    @Provides
    @IntoSet
    @Singleton
    fun provide2To3Migration(): Migration {
        return object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.run {
                    try {
                        beginTransaction()   // Create temp posts table
                        execSQL(
                            """CREATE TABLE IF NOT EXISTS PostEntityTmp (subTitle TEXT  NULL, imageUrl TEXT NULL, 
                            id INTEGER NOT NULL, title TEXT  NULL, content TEXT NULL, sourceUrl TEXT NULL, notification INTEGER NOT NULL,
                            updatedAt TEXT NULL, videoUrl TEXT NULL, commentsCount TEXT NULL,  sourceId INTEGER NULL, source TEXT NULL, catImageUrl TEXT NULL, catId INTEGER NULL, 
                            category TEXT NULL,topicUpdatedAt TEXT NULL, PRIMARY KEY(id))""".trimIndent()
                        )

                        execSQL(
                            """INSERT INTO PostEntityTmp (subTitle, imageUrl, id, title, content, sourceUrl,notification,updatedAt,videoUrl,commentsCount,
                            sourceId,source,catImageUrl,catId,category,topicUpdatedAt)
                            SELECT subTitle, imageUrl, id, title, content, sourceUrl,notification,updatedAt,null,null,
                            sourceId,source,catImageUrl,catId,category,topicUpdatedAt FROM PostEntity""".trimIndent()
                        )

                        // Remove the old table
                        execSQL("DROP TABLE PostEntity")

                        // Change the table name to the correct one
                        execSQL("ALTER TABLE PostEntityTmp RENAME TO PostEntity")
                        setTransactionSuccessful()
                    } finally {
                        endTransaction()
                    }
                }
            }
        }
    }

    /*
     * We need the TopicsDao module.
     * For this, We need the AppDatabase object
     * So we will define the providers for this here in this module.
     * */
    @Provides
    @Singleton
    internal fun provideTopicsDao(appDatabase: AppDatabase): TopicDao {
        return appDatabase.topicsDao()
    }

    @Provides
    @Singleton
    internal fun providePostsDao(appDatabase: AppDatabase): PostDao {
        return appDatabase.postDao()
    }
}

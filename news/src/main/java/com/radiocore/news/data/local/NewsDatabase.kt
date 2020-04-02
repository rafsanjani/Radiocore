package com.radiocore.news.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.radiocore.news.model.News
import com.radiocore.news.util.NewsConverters


@Database(entities = [News::class], version = 1)
@TypeConverters(NewsConverters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var instance: NewsDatabase? = null

        @Synchronized
        fun getInstance(context: Context): NewsDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, NewsDatabase::class.java, "RadioCoreDB")
                        .fallbackToDestructiveMigration().build()
            }

            return instance!!
        }
    }
}